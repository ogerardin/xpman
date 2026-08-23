package com.ogerardin.xpman;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.shell.Section;
import com.ogerardin.xpman.shell.SidebarController;
import com.ogerardin.xpman.util.JsonFileConfigPersister;
import com.ogerardin.xpman.util.jfx.JfxApp;
import com.ogerardin.xpman.util.jfx.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    @FXML
    private MenuItem themeMenuItem;

    @FXML
    private StackPane contentArea;

    @FXML
    private SidebarController sidebarController;

    private final Map<Section, Node> sectionCache = new EnumMap<>(Section.class);

    @Getter
    private final JsonFileConfigPersister<XPManPrefs> configManager = new JsonFileConfigPersister<>(XPManPrefs.class, ".xpman");

    @Getter(lazy = true)
    private final XPManPrefs config = configManager.getConfig();

    @Getter
    private final ThemeManager themeManager = new ThemeManager(getConfig(), this::saveConfig);

    private static final XPlaneProperty xPlaneProperty = new XPlaneProperty();

    public XPlaneProperty xPlaneProperty() {
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
                    .forEach(log::debug);
        } else {
            // just dump a few selected properties
            Stream.of("java.vendor", "java.version", "os.arch", "os.name", "os.version")
                    .map(propertyName -> String.format("  %s: %s", propertyName, System.getProperty(propertyName)))
                    .forEach(log::info);
        }

        System.setProperty("prism.lcdtext", "false");
        System.setProperty("prism.text", "t2k");

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
        if (event.getType() == ManagerEvent.Type.LOADED) {
            List<MenuItem> menuItems = event.getItems().stream()
                    .filter(InstalledTool.class::isInstance)
                    .map(InstalledTool.class::cast)
                    .map(this::newToolMenuItem)
                    .collect(Collectors.toList());
            Platform.runLater(() -> toolsMenu.getItems().setAll(menuItems));
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
        updateThemeMenuItem();
        sidebarController.selectedSectionProperty().addListener((__, ___, section) ->
                Optional.ofNullable(section).ifPresent(this::showSection));
        sidebarController.select(Section.HOME);
        XPManPrefs config = getConfig();
        if (config.getLastXPlanePath() != null) {
            Platform.runLater(() -> openXPlane(Paths.get(config.getLastXPlanePath()).toFile()));
        } else {
            Platform.runLater(this::open);
        }
    }

    /**
     * Displays the content of the given section in the main content area, loading and caching
     * the corresponding view on first access.
     */
    @SneakyThrows
    private void showSection(Section section) {
        Node content = sectionCache.computeIfAbsent(section, this::loadSectionContent);
        if (contentArea.getChildren().isEmpty() || contentArea.getChildren().get(0) != content) {
            contentArea.getChildren().setAll(content);
        }
    }

    @SneakyThrows
    private Node loadSectionContent(Section section) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(section.getContentFxml()));
        loader.setControllerFactory(this::buildController);
        return loader.load();
    }

    /**
     * Registers Alt+1..6 and Shortcut+1..6 accelerators for direct section navigation.
     */
    private void installSectionAccelerators(Scene scene) {
        Section[] sections = Section.values();
        for (int i = 0; i < sections.length; i++) {
            KeyCode code = KeyCode.valueOf("DIGIT" + (i + 1));
            Section section = sections[i];
            scene.getAccelerators().put(
                    new KeyCodeCombination(code, KeyCombination.ALT_DOWN),
                    () -> sidebarController.select(section));
            scene.getAccelerators().put(
                    new KeyCodeCombination(code, KeyCombination.SHORTCUT_DOWN),
                    () -> sidebarController.select(section));
        }
    }

    @FXML
    private void toggleTheme() {
        getThemeManager().toggle();
        updateThemeMenuItem();
    }

    private void updateThemeMenuItem() {
        if (themeMenuItem != null) {
            themeMenuItem.setText(getThemeManager().isDark()
                    ? "Switch to Light Theme"
                    : "Switch to Dark Theme");
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

        getThemeManager().applySavedTheme();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        loader.setControllerFactory(this::buildController);
        Pane mainPane = loader.load();
        Scene scene = new Scene(mainPane);
        scene.getStylesheets().add(getClass().getResource("/css/xpman.css").toExternalForm());
//        scene.getRoot().setStyle("-fx-font-family: 'sans-serif'");
        stage.setScene(scene);

        installSectionAccelerators(scene);

        stage.setMinWidth(900);
        stage.setMinHeight(600);

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

    @FXML
    public void manageTools() {
        sidebarController.select(Section.TOOLS);
    }

    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}