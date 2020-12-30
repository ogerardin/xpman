package com.ogerardin.xpman;

import com.ogerardin.xplane.XPlaneInstance;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xpman.config.PrefsConfigManager;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.JfxApp;
import javafx.application.Platform;
import javafx.beans.value.ObservableObjectValue;
import javafx.event.ActionEvent;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class XPmanFX extends JfxApp<XPManPrefs> {

    @FXML
    private MenuBar mainMenu;

    @FXML
    private Menu recentMenu;

    @Getter(value = AccessLevel.PROTECTED, lazy = true)
    private final XPManPrefs config = PrefsConfigManager.load();

    private static final XPlaneInstanceProperty xPlaneInstanceProperty = new XPlaneInstanceProperty();
    public ObservableObjectValue<XPlaneInstance> xPlaneInstanceProperty() {
        return xPlaneInstanceProperty;
    }


    public static void main(String[] args) {
        // catch-all exception handler (text version)
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> log.error("Caught exception", e));

        String version = XPmanFX.class.getPackage().getImplementationVersion();
        log.info("Starting X-Plane Manager version {}", Optional.ofNullable(version).orElse("Unknown"));
        if (isDevMode()) {
            // dump all System properties
            System.getProperties().entrySet().stream()
                    .sorted(Comparator.comparing(entry -> (String) entry.getKey()))
                    .map(entry -> String.format("  %s: %s", entry.getKey(), entry.getValue()))
                    .forEach(log::info);
        } else {
            // just dump a few selected properties
            Stream.of("java.vendor", "java.version", "os.arch", "os.name", "os.version")
                    .map(propertyName -> String.format("  %s: %s", propertyName, System.getProperty(propertyName)))
                    .forEach(log::info);
        }

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

        if ((xplane.getVariant() == XPlaneVariant.UNKNOWN) && !isDevMode()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, String.format("%s is not a valid X-Plane folder.", folder.toString()));
            alert.initOwner(primaryStage);
            alert.showAndWait();
            return;
        }
        xPlaneInstanceProperty.set(xplane);

        XPManPrefs config = getConfig();
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
        XPManPrefs config = getConfig();
        if (config.getLastXPlanePath() != null) {
            Platform.runLater(() -> openXPlane(Paths.get(config.getLastXPlanePath()).toFile()));
        }
    }

    private void updateRecent() {
        final XPManPrefs config = getConfig();
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
    protected void saveConfig(XPManPrefs config) {
        PrefsConfigManager.save(config);
    }

    @FXML
    private void installWizard(ActionEvent actionEvent) {
        InstallWizard wizard = new InstallWizard(xPlaneInstanceProperty().getValue());
        wizard.showAndWait();
    }

    @FXML
    public void newIssue(ActionEvent actionEvent) throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman/issues/new"));
    }

    @FXML
    public void github(ActionEvent actionEvent) throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman"));
    }

    @FXML
    public void help(ActionEvent actionEvent) throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman/wiki"));
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}