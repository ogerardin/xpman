package com.ogerardin.xpman;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.JsonFileConfigPersister;
import com.ogerardin.xpman.util.jfx.JfxApp;
import javafx.application.Platform;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
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
    private Menu toolsMenu;

    @FXML
    private MenuBar mainMenu;

    @FXML
    private Menu recentMenu;

    @Getter
    private final JsonFileConfigPersister<XPManPrefs> configManager = new JsonFileConfigPersister<>(XPManPrefs.class);

    @Getter(lazy = true)
    private final XPManPrefs config = configManager.getConfig();

    private static final XPlaneProperty xPlaneProperty = new XPlaneProperty();

    public ObservableObjectValue<XPlane> xPlaneProperty() {
        return xPlaneProperty;
    }


    public static void main(String[] args) {
        // catch-all exception handler (text version)
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> log.error("Caught exception", e));

//        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        root.setLevel(Level.DEBUG);

        String version = XPmanFX.class.getPackage().getImplementationVersion();
        log.info("Starting X-Plane Manager version {}", Optional.ofNullable(version).orElse("Unknown"));
        if (log.isDebugEnabled()) {
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
        directoryChooser.setTitle("Please select X-Plane directory");
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
        XPlane xplane = new XPlane(folder);

        if ((xplane.getVariant() == XPlaneVariant.UNKNOWN)) {
            Alert alert = new Alert(Alert.AlertType.ERROR, String.format("%s is not a valid X-Plane folder.", folder));
            alert.setHeaderText("Invalid X-Plane folder");
            alert.initOwner(primaryStage);
            alert.showAndWait();
            return;
        }
        xPlaneProperty.set(xplane);

        XPManPrefs config = getConfig();
        config.setLastXPlanePath(folder.toString());
        config.getRecentPaths().add(folder.toString());
        saveConfig();

        updateRecent();

        updateTools();
    }

    private void updateTools() {
        ToolsManager toolsManager = xPlaneProperty().get().getToolsManager();
        toolsManager.registerListener(this::handleToolsEvent);
        toolsManager.reload();
    }

    private void handleToolsEvent(ManagerEvent<Tool> event) {
        if (event instanceof ManagerEvent.Loaded<Tool> loadedEvent) {
            List<MenuItem> menuItems = loadedEvent.getItems().stream()
                    .filter(InstalledTool.class::isInstance)
                    .map(InstalledTool.class::cast)
                    .map(this::newToolMenuItem)
                    .collect(Collectors.toList());
            toolsMenu.getItems().setAll(menuItems);
        }
    }

    private MenuItem newToolMenuItem(InstalledTool tool) {
        MenuItem menuItem = new MenuItem(tool.getName());
        menuItem.setOnAction(event -> Platforms.getCurrent().startApp(tool.getApp()));
        return menuItem;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private <C> C buildController(Class<C> type) {
        if (type == this.getClass()) {
            // don't reinstantiate this class, use the existing instance
            return (C) this;
        }
        try {
            // if the controller class has a constructor that takes a XPmanFX parameter, use it
            Constructor<C> constructor = type.getConstructor(XPmanFX.class);
            return constructor.newInstance(this);
        } catch (NoSuchMethodException e) {
            // otherwise use no-arg constructor
            return type.getConstructor().newInstance();
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
        } else {
            Platform.runLater(this::open);
        }
    }

    private void updateRecent() {
        final XPManPrefs config = getConfig();
        List<? extends MenuItem> menuItems = config.getRecentPaths().stream()
                .map(RecentMenuItem::new)
                .toList();
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
        Scene scene = new Scene(mainPane);
//        scene.getRoot().setStyle("-fx-font-family: 'sans-serif'");
        stage.setScene(scene);

        restoreWindowPosition(stage);
    }

    @Override
    protected void saveConfig() {
        configManager.save();
    }

    @FXML
    private void installWizard() {
        InstallWizard wizard = new InstallWizard(xPlaneProperty().getValue());
        wizard.showAndWait();
    }

    @FXML
    public void newIssue() throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman/issues/new"));
    }

    @FXML
    public void github() throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman"));
    }

    @FXML
    public void help() throws MalformedURLException {
        Platforms.getCurrent().openUrl(new URL("https://github.com/ogerardin/xpman/wiki"));
    }

    @SneakyThrows
    @FXML
    public void manageTools() {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/tools/tools.fxml"));
        loader.setControllerFactory(this::buildController);
        Pane pane = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Tools Manager");
        stage.setScene(new Scene(pane));
        stage.initOwner(primaryStage);
        stage.show();
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}