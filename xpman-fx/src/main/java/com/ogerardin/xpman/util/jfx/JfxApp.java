package com.ogerardin.xpman.util.jfx;

import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

/**
 * Superclass for a JavaFX app. Provides window position restore, preferences management with {@link JfxAppPrefs},
 * quit confirmation, etc.
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

        //FIXME native mac menu exhibits some problems...
//        if (Platforms.getCurrent() == Platforms.MAC) {
//            setMacNativeMenu(primaryStage);
//        }

        primaryStage.show();
        log.debug("Ready!");
    }

    /**
     * If the primary stage's scene is a Pane and it has a MenuBar, move it to the Mac's native menu bar
     */
    private static void setMacNativeMenu(Stage primaryStage) {
        MenuToolkit tk = MenuToolkit.toolkit();
        if (primaryStage.getScene().getRoot() instanceof Pane root) {
            for (Node child : root.getChildren()) {
                if (child instanceof MenuBar menuBar) {
                    tk.setMenuBar(root, menuBar);
                    break;
                }
            }
        }
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
