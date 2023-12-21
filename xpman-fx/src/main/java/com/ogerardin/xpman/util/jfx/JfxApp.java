package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xplane.util.platform.Platforms;
import de.jangassen.MenuToolkit;
import de.jangassen.model.AppearanceMode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

/**
 * Superclass for a JavaFX app. Provides window position restore, preferences management with {@link JfxAppPrefs}, quit
 * confirmation, etc.
 *
 * @param <C> specific preferences class
 */
@Slf4j
public abstract class JfxApp<C extends JfxAppPrefs> extends Application {

    @Getter
    protected Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // catch-all exception handler (GUI version)
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> Platform.runLater(() -> ErrorDialog.showError(throwable, primaryStage)));

        log.debug("Setting up stage");
        setupStage(primaryStage);

        // if we're on a Mac, use native Mac application menu
        if (Platforms.getCurrent() == Platforms.MAC) {
            setMacNativeMenu(primaryStage);
        }

        primaryStage.show();
        log.debug("Ready!");
    }

    /**
     * Use the specified Stage's MenuBar (if any) as the native Mac application menu.
     * Note: the first menu will always be renamed with the application name ("java" when run as a command line
     * or the value of the CFBundleName key in the info.plist file when run as a bundled Mac app)
     */
    private static void setMacNativeMenu(Stage stage) {
        MenuToolkit tk = MenuToolkit.toolkit();
        tk.setAppearanceMode(AppearanceMode.AUTO);
        if (stage.getScene().getRoot() instanceof Pane root) {
            root.getChildren().stream()
                    .filter(child -> child instanceof MenuBar)
                    .map(MenuBar.class::cast)
                    .findFirst()
                    .map(JfxApp::fixShortcuts)
                    .ifPresent(menuBar -> tk.setMenuBar(root, menuBar));
        }
    }

    /**
     * Fix accelerators to work around issue https://github.com/0x4a616e/NSMenuFX/issues/24
     * This is done by forcing the "meta" modifier to use the value of the "shortcut" modifier.
     */
    private static MenuBar fixShortcuts(MenuBar menuBar) {
        for (Menu menu : menuBar.getMenus()) {
            for (MenuItem item : menu.getItems()) {
                KeyCombination accelerator = item.getAccelerator();
                if (accelerator instanceof KeyCodeCombination kcc) {
                    item.setAccelerator(new KeyCodeCombination(kcc.getCode(),
                            kcc.getShift(),
                            kcc.getControl(),
                            kcc.getAlt(),
                            /* meta = */ kcc.getShortcut(),
                            /* shortcut = */ KeyCombination.ModifierValue.ANY
                    ));
                }
            }

        }
        return menuBar;
    }

    protected abstract void setupStage(Stage primaryStage);

    protected void restoreWindowPosition(Stage stage) {
        final JfxAppPrefs.WindowPosition lastPosition = getConfig().getLastPosition();
        if (lastPosition != null) {
            stage.setX(lastPosition.getX());
            stage.setY(lastPosition.getY());
            stage.setWidth(lastPosition.getWidth());
            stage.setHeight(lastPosition.getHeight());
        }
    }

    protected abstract C getConfig();

    protected abstract void saveConfig();

    private void saveWindowPosition(Stage stage) {
        final JfxAppPrefs.WindowPosition position = new JfxAppPrefs.WindowPosition(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        final C config = getConfig();
        config.setLastPosition(position);
        saveConfig();
    }

    @FXML
    protected void quit() {
        Alert alert = new Alert(CONFIRMATION, "Do you really want to quit?");
        alert.initOwner(primaryStage);
        alert.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .ifPresent(buttonType -> quitNow());
    }

    private void quitNow() {
        saveWindowPosition(primaryStage);
        Platform.exit();
        System.exit(0);
    }
}
