package com.ogerardin.xpman.util.jfx.wizard;

import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An improved wizard based on controlfx's {@link org.controlsfx.dialog.Wizard}. Main differences are:
 * <ul>
 *     <li>Method {@link #setFlow(String...)} available to set a simple linear flow from a list of FXML pages</li>
 *     <li>When loading a page, if the page's controller class has a constructor that takes a {@link Wizard} parameter,
 *     then use it (passing this) instead of the no-args constructor.</li>
 *     <li>If the WizardPane is an instance of {@link ValidatingWizardPane} and its controller implements {@link Validating},
 *     then the WizardPane's {@code invalidProperty} will be set to the controller's {@link Validating#invalidProperty()}.
 *     As a consequence, the "Next" button's availability will be linked to the page's validity status, which itself may be
 *     controlled through {@link org.controlsfx.validation.ValidationSupport}</li>
 *     <li>If the WizardPane is an instance of {@link ValidatingWizardPane} and its controller implements {@link PageListener},
 *     then the controller will be registered as a {@code PageListener}, which means that its {@link PageListener#onEnteringPage}
 *     and {@link PageListener#onExitingPage} methods will be called when the page is displayed/left.</li>
 *     <li>Wizard window is resizable, can persist its size between sessions,
 *     and keeps its window bounds stable when navigating between pages (ControlsFX's resize-on-page-change is neutralised).
 *     The window position is not persisted: the dialog always opens centered (ControlsFX's dialog
 *     force-centers it on every show, see HeavyweightDialog.showAndWait)</li>
 * </ul>
 */
@Slf4j
@SuppressWarnings("java:S2176") // Deliberately shadows ControlsFX Wizard to provide app-specific behavior.
public class Wizard extends org.controlsfx.dialog.Wizard {

    public Wizard(String title) {
        setTitle(title);
        getDialog().setResizable(true);

        // Neutralise the resize+recenter applied on every page change: keep the window bounds the user sees.
        // Page swap sequence: ControlsFX updatePage → Dialog.setDialogPane(next) → dialogPaneProperty.invalidated()
        // → HeavyweightDialog.setDialogPane → scene.setRoot(next) [fires root listeners HERE, before the resize]
        // → dialogPane.autosize() + stage.sizeToScene() [RESIZE #1] → back in ControlsFX: sizeToScene() [RESIZE #2]
        // + recenter. External dialogPaneProperty listeners fire only AFTER invalidated() (ObjectPropertyBase
        // semantics), i.e. after RESIZE #1 — too late. The scene's root listener is the one hook that fires
        // BEFORE the resize: snapshot the pre-change bounds here and restore them via runLater after the whole
        // synchronous page-change handler (resizes + recenter) completes.
        Scene scene = getDialog().getDialogPane().getScene();
        if (scene != null) {
            scene.rootProperty().addListener((_, _, _) -> {
                if (!(scene.getWindow() instanceof Stage stage)) return;
                double x = stage.getX(), y = stage.getY(), w = stage.getWidth(), h = stage.getHeight();
                // Pre-show pane swaps and hide teardown snapshot an unpositioned stage (NaN bounds);
                // restoring those would poison the bounds — only restore real bounds (page changes).
                if (Double.isNaN(x + y + w + h)) return;
                Platform.runLater(() -> {
                    stage.setX(x); stage.setY(y); stage.setWidth(w); stage.setHeight(h);
                });
            });
        }
    }

    /**
     * Sets up size persistence for this wizard. Position is not persisted: the dialog always opens centered.
     *
     * @param sizeLoader function to load the saved size (can return null)
     * @param sizeSaver function to save the current size
     */
    public void setupSizePersistence(Supplier<JfxAppPrefs.WindowSize> sizeLoader,
                                     Consumer<JfxAppPrefs.WindowSize> sizeSaver) {

        getDialog().setOnShowing(event -> {
            Stage stage = getStage();
            if (stage == null) return;
            JfxAppPrefs.WindowSize size = sizeLoader.get();
            if (size != null && !Double.isNaN(size.width() + size.height())) {
                stage.setWidth(size.width());
                stage.setHeight(size.height());
            }
        });

        // save on HIDING, not HIDDEN: by then the dialog pane's scene is still attached (at
        // HIDDEN getStage() returns null and nothing could ever be saved)
        getDialog().setOnHiding(event -> {
            Stage stage = getStage();
            if (stage == null) return;
            double w = stage.getWidth(), h = stage.getHeight();
            // never persist a NaN size: it would poison the prefs file and make every
            // subsequent Gson save throw ("JSON forbids NaN and infinities")
            if (Double.isNaN(w + h)) return;
            sizeSaver.accept(new JfxAppPrefs.WindowSize(w, h));
        });
    }

    private Stage getStage() {
        if (getDialog().getDialogPane() != null &&
            getDialog().getDialogPane().getScene() != null &&
            getDialog().getDialogPane().getScene().getWindow() instanceof Stage) {
            return (Stage) getDialog().getDialogPane().getScene().getWindow();
        }
        return null;
    }

    /**
     * Set this Wizard's flow as a {@link org.controlsfx.dialog.Wizard.LinearFlow} of {@link WizardPane}s
     * instantiated from the specified FXML resources.
     *
     * @param pages list of FXML resources; each of them must define a WizardPane (or subclass)
     */
    protected void setFlow(String... pages) {
        WizardPane[] wizardPanes = Stream.of(pages)
                .map(this::loadWizardPane)
                .toArray(WizardPane[]::new);
        final LinearFlow flow = new LinearFlow(wizardPanes);
        setFlow(flow);
    }

    @SneakyThrows
    private WizardPane loadWizardPane(String page) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Wizard.class.getResource(page));
        loader.setControllerFactory(this::buildController);
        WizardPane wizardPane = loader.load();
        wizardPane.getStylesheets().add(getClass().getResource("/css/xpman.css").toExternalForm());

        Object controller = loader.getController();
        if (wizardPane instanceof ValidatingWizardPane validatingWizardPane) {
            if (controller instanceof Validating validatingController) {
                validatingWizardPane.setInvalidProperty(validatingController.invalidProperty());
            }
            if (controller instanceof PageListener pageListenerController) {
                validatingWizardPane.setPageListener(pageListenerController);
            }
        }
        return wizardPane;
    }

    @SneakyThrows
    private <C> C buildController(Class<C> type) {
        try {
            // if the controller class has a constructor that takes a Wizard parameter, use it
            Constructor<C> constructor = type.getConstructor(this.getClass());
            return constructor.newInstance(this);
        } catch (NoSuchMethodException _) {
            // otherwise use no-arg constructor
            return type.getConstructor().newInstance();
        }
    }

    public static void disableButton(WizardPane wizardPane, ButtonBar.ButtonData buttonData, boolean disabled) {
        wizardPane.getButtonTypes().stream()
                .filter(buttonType -> buttonType.getButtonData().equals(buttonData))
                .map(wizardPane::lookupButton)
                .forEach(node -> Platform.runLater(() -> node.setDisable(disabled)));
    }

}
