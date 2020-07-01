package com.ogerardin.xpman;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.config.Config;
import com.ogerardin.xpman.config.ConfigManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javafx.scene.control.Alert.AlertType.CONFIRMATION;

@Slf4j
public class XPmanFX extends Application {

    @FXML
    private VBox main;

    @FXML
    private Menu recentMenu;

    @FXML
    private TabPane tabPane;

    private Stage primaryStage;

    private static final XPlaneInstanceProperty xPlaneInstanceProperty = new XPlaneInstanceProperty();

    private final Config config = ConfigManager.INSTANCE.load();

    public XPmanFX() {
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        log.debug("Setting up stage");
        setupStage(primaryStage);
        primaryStage.show();
        log.debug("Ready!");
    }


    @SneakyThrows
    private void setupStage(Stage stage) {
        stage.setTitle("XPman");
        stage.setOnCloseRequest(windowEvent -> {
            windowEvent.consume();
            quit();
        });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(this::buildController);
        Pane mainPane = loader.load();
        stage.setScene(new Scene(mainPane));

        restoreWindowPosition(stage);

    }

    private void restoreWindowPosition(Stage stage) {
        Preferences prefs = getPrefsRoot();
        stage.setX(prefs.getDouble("X", 10));
        stage.setY(prefs.getDouble("Y", 10));
        stage.setWidth(prefs.getDouble("W", main.getPrefWidth()));
        stage.setHeight(prefs.getDouble("H", main.getPrefHeight()));
    }

    private Preferences getPrefsRoot() {
        return Preferences.userRoot().node("XPMan");
    }

    private void saveWindowPosition(Stage stage) {
        Preferences prefs = getPrefsRoot();
        prefs.putDouble("X", stage.getX());
        prefs.putDouble("Y", stage.getY());
        prefs.putDouble("W", stage.getWidth());
        prefs.putDouble("H", stage.getHeight());
    }

    public static void main(String[] args) {
        String version = XPmanFX.class.getPackage().getImplementationVersion();
        log.info("Starting X-Plane Manager version {}", Optional.ofNullable(version).orElse("Unknown"));
        Stream.of("java.vendor", "java.version", "os.arch", "os.name", "os.version")
                .map(propertyName -> String.format("  %s: %s", propertyName, System.getProperty(propertyName)))
                .forEach(log::info);
        launch(args);
    }

    @FXML
    protected void quit() {
        Alert alert = new Alert(CONFIRMATION, "Do you really want to quit?");
        alert.showAndWait()
                .filter(buttonType -> buttonType == ButtonType.OK)
                .ifPresent(buttonType -> {
                    saveWindowPosition(primaryStage);
                    Platform.exit();
                    System.exit(0);
                });
    }

    @FXML
    public void open() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);
        if (selectedDirectory == null) {
            return;
        }
        openXPlane(selectedDirectory);
    }

    @SneakyThrows
    private void openXPlane(File selectedDirectory) {
        Path folder = selectedDirectory.toPath().toRealPath();
        log.info("Opening X-Plane folder {}", folder);
        XPlaneInstance xplane = new XPlaneInstance(folder);
        xPlaneInstanceProperty.set(xplane);
        config.setLastXPlanePath(folder.toString());
        config.getRecentPaths().add(folder.toString());
        ConfigManager.INSTANCE.save(config);
        updateRecent();
    }

    @SneakyThrows
    private <C> C buildController(Class<C> type) {
        if (type == this.getClass()) {
            // don't reinstantiate this class, use the existing instance
            //noinspection unchecked
            return (C) this;
        }
        try {
            // if the controller class has a constuctor that takes a XPlaneInstanceProperty, use it
            Constructor<C> constructor = type.getConstructor(XPlaneInstanceProperty.class);
            return constructor.newInstance(xPlaneInstanceProperty);
        } catch (NoSuchMethodException e) {
            // otherwise use no-arg constructor
            return type.newInstance();
        }
    }

    @SneakyThrows
    public void about() {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/about.fxml"));
        dialog.setDialogPane(loader.load());
        dialog.show();
    }

    @FXML
    private void initialize() {
        updateRecent();
    }

    private void updateRecent() {
        List<MenuItem> menuItems = config.getRecentPaths().stream()
                .map(RecentMenuItem::new)
                .collect(Collectors.toList());
        recentMenu.getItems().setAll(menuItems);
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}