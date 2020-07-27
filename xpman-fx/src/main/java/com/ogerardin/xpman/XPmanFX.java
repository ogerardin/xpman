package com.ogerardin.xpman;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.config.XPManConfig;
import com.ogerardin.xpman.config.PrefsConfigManager;
import com.ogerardin.xpman.util.jfx.JfxApp;
import javafx.application.Platform;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class XPmanFX extends JfxApp<XPManConfig> {

    @FXML
    private Menu recentMenu;

    @Getter(value = AccessLevel.PROTECTED, lazy = true)
    private final XPManConfig config = PrefsConfigManager.load();

    private static final XPlaneInstanceProperty xPlaneInstanceProperty = new XPlaneInstanceProperty();
    public ObservableObjectValue<XPlaneInstance> xPlaneInstanceProperty() {
        return xPlaneInstanceProperty;
    }


    public static void main(String[] args) {
        // catch-all exception handler (text version)
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> log.error("Caught exception", e));

        String version = XPmanFX.class.getPackage().getImplementationVersion();
        log.info("Starting X-Plane Manager version {}", Optional.ofNullable(version).orElse("Unknown"));
        Stream.of("java.vendor", "java.version", "os.arch", "os.name", "os.version")
                .map(propertyName -> String.format("  %s: %s", propertyName, System.getProperty(propertyName)))
                .forEach(log::info);

        // fire up JavaFX. This will instantiate a XPmanFX and call #start
        launch(args);
    }

    @FXML
    private void open() {
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

        XPManConfig config = getConfig();
        config.setLastXPlanePath(folder.toString());
        config.getRecentPaths().add(folder.toString());
        saveConfig(config);

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
            // if the controller class has a constructor that takes a XPmanFX parameter, use it
            Constructor<C> constructor = type.getConstructor(XPmanFX.class);
            return constructor.newInstance(this);
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
        dialog.initOwner(primaryStage);
        dialog.show();
    }

    @FXML
    private void initialize() {
        updateRecent();
    }

    private void updateRecent() {
        final XPManConfig config = getConfig();
        List<MenuItem> menuItems = config.getRecentPaths().stream()
                .map(RecentMenuItem::new)
                .collect(Collectors.toList());
        recentMenu.getItems().setAll(menuItems);
    }

    @SneakyThrows
    protected void setupStage(Stage stage) {
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

    @Override
    protected void saveConfig(XPManConfig config) {
        PrefsConfigManager.save(config);
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}