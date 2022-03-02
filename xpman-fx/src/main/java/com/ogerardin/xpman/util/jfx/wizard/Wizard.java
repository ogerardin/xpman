package com.ogerardin.xpman.util.jfx.wizard;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.lang.reflect.Constructor;
import java.util.stream.Stream;

/**
 * An improved wizard based on controlfx's {@link org.controlsfx.dialog.Wizard}. Main differences are:
 * <ul>
 *     <li>Method {@link #setFlow(String...)} available to set a simple linear flow from a list of FXML pages</li
 *     <li>When loading a page, if the page's controller class has a constructor that takes a {@link Wizard} parameter,
 *     then use it (passing this) instead of the no-args constructor.</li>
 *     <li>If the WizardPane is an instance of {@link ValidatingWizardPane} and its controller implements {@link Validating},
 *     then the WizardPane's {@code invalidProperty} will be set to the controller's {@link Validating#invalidProperty()}.
 *     As a consequence, the "Next" button's availability will be linked to the page's validity status, which itself may be
 *     controlled through {@link org.controlsfx.validation.ValidationSupport}</li>
 *     <li>If the WizardPane is an instance of {@link ValidatingWizardPane} and its controller implements {@link PageListener},
 *     then the controller will be registered as a {@code PageListener}, which means that its {@link PageListener#onEnteringPage}
 *     and {@link PageListener#onExitingPage} methods will be called when the page is displayed/left.</li>
 * </ul>
 */
@Slf4j
public class Wizard extends org.controlsfx.dialog.Wizard {

    public Wizard(String title) {
        setTitle(title);
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
        } catch (NoSuchMethodException e) {
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
