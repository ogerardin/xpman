package com.ogerardin.xpman.util.jfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
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
    private static final boolean devMode = Boolean.parseBoolean(System.getProperty("jfxapp.devmode", "false"));

    static {
        log.debug("dev mode: {}", devMode);
    }

    @Getter
    protected Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // catch-all exception handler (GUI version)
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> ErrorDialog.showError(throwable, primaryStage));

        log.debug("Setting up stage");
        setupStage(primaryStage);
        primaryStage.show();
        log.debug("Ready!");
    }

    protected abstract void setupStage(Stage primaryStage);

    protected void restoreWindowPosition(Stage stage) {
        final Rectangle2D lastPosition = getConfig().getLastPosition();
        if (lastPosition != null) {
            stage.setX(lastPosition.getMinX());
            stage.setY(lastPosition.getMinY());
            stage.setWidth(lastPosition.getWidth());
            stage.setHeight(lastPosition.getHeight());
        }
    }

    protected abstract C getConfig();

    protected abstract void saveConfig(C config);

    private void saveWindowPosition(Stage stage) {
        final Rectangle2D position = new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        final C config = getConfig();
        config.setLastPosition(position);
        saveConfig(config);
    }

    @FXML
    protected void quit() {
        if (isDevMode()) {
            quitNow();
        }

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
