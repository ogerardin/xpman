package com.ogerardin.xpman;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.settings.SettingsCategory;
import com.ogerardin.xpman.settings.SettingsController;
import com.ogerardin.xpman.shell.Section;
import com.ogerardin.xpman.shell.SidebarController;
import com.ogerardin.xpman.util.JsonFileConfigPersister;
import com.ogerardin.xpman.util.Logs;
import com.ogerardin.xpman.util.jfx.JfxApp;
import com.ogerardin.xpman.util.jfx.JfxAppPrefs;
import com.ogerardin.xpman.util.jfx.ThemeManager;
import com.ogerardin.xpman.util.jfx.WindowPositionHelper;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Main application class for X-Plane Manager (XPman), a JavaFX desktop application for managing
 * X-Plane installations, aircraft, plugins, scenery, and navigation data.
 *
 * <p>Extends {@link JfxApp} with {@link XPManPrefs} for persistent user preferences stored in
 * a {@code ~/.xpman} JSON dotfile. Sets up the primary stage with a sidebar-driven content area,
 * a menu bar with recent-X-Plane paths, theme management, and keyboard accelerators (Alt/Cmd+1..5)
 * for direct section navigation.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Loads the main FXML layout and wires the {@link SidebarController} to swap section content
 *       (cached per {@link Section} to avoid redundant FXML loads).</li>
 *   <li>Provides {@link #buildController(Class)} as the controller factory for all FXML loaders,
 *       instantiating controllers via a {@code (XPmanFX)} constructor when available, falling back
 *       to a no-arg constructor, and returning {@code this} for the main controller type.</li>
 *   <li>Holds the shared {@link XPlaneProperty} (the currently open X-Plane instance) and the
 *       {@link SceneryOrganizer} for scenery classification.</li>
 *   <li>Handles opening X-Plane directories (with validation, recent-path tracking, and stale-path
 *       cleanup), launching the install wizard, settings dialog, log export, and external links.</li>
 *   <li>Registers tools discovered by the {@link ToolsManager} into the Tools menu.</li>
 * </ul>
 *
 * <p>The {@link #main(String[])} entry point installs a default uncaught-exception handler, logs
 * version and environment info, tweaks text rendering system properties, and calls {@link #launch}.
 */
@Slf4j
public class XPmanFX extends JfxApp<XPManPrefs> {

    @FXML
    private Menu toolsMenu;

    @FXML
    private MenuItem manageToolsMenuItem;

    @FXML
    private CheckMenuItem debugLoggingItem;

    @FXML
    private SeparatorMenuItem toolsMenuSeparator;

    @FXML
    private MenuBar mainMenu;

    @FXML
    private Menu recentMenu;

    @FXML
    private StackPane contentArea;

    @FXML
    private SidebarController sidebarController;

    private final Map<Section, Node> sectionCache = new EnumMap<>(Section.class);

    private final Map<Section, Object> sectionControllers = new EnumMap<>(Section.class);

    @Getter
    private final JsonFileConfigPersister<XPManPrefs> configManager = new JsonFileConfigPersister<>(XPManPrefs.class, ".xpman");

    @Getter(lazy = true)
    private final XPManPrefs config = configManager.getConfig();

    @Getter
    private final ThemeManager themeManager = new ThemeManager(getConfig(), this::saveConfig);

    @Getter
    private final SceneryOrganizer sceneryOrganizer = new SceneryOrganizer(getConfig().getSceneryClasses());

    // Bridge the user preference into Skunkcrafts availability; read live so Settings changes apply without restart
    {
        SkunkcraftsUpdater.ignoreBetaChannel = () -> getConfig().isIgnoreBetaUpdates();
    }

    private static final XPlaneProperty xPlaneProperty = new XPlaneProperty();

    public XPlaneProperty xPlaneProperty() {
        return xPlaneProperty;
    }

    public static void main(String[] args) {
        // catch-all exception handler (text version)
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> log.error("Caught exception", e));

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
    public void open() {
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
        Path folder = selectedDirectory.toPath();
        if (!Files.exists(folder)) {
            String stalePath = folder.toString();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    String.format("The folder %s no longer exists. Remove it from the recent paths list?", folder));
            alert.setHeaderText("Folder not found");
            alert.initOwner(primaryStage);
            alert.showAndWait()
                    .filter(buttonType -> buttonType == ButtonType.OK)
                    .ifPresent(buttonType -> {
                        getConfig().getRecentPaths().remove(stalePath);
                        saveConfig();
                        updateRecent();
                    });
            return;
        }
        folder = folder.toRealPath();
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

        XPManPrefs prefs = getConfig();
        prefs.setLastXPlanePath(folder.toString());
        prefs.getRecentPaths().add(folder.toString());
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
                    .toList();
            Platform.runLater(() -> toolsMenu.getItems().setAll(
                    Stream.concat(Stream.of(manageToolsMenuItem, toolsMenuSeparator), menuItems.stream()).toList()));
        }
    }

    private MenuItem newToolMenuItem(InstalledTool tool) {
        MenuItem menuItem = new MenuItem(tool.getName());
        menuItem.setOnAction(event -> Platforms.getCurrent().startApp(tool.getApp()));
        return menuItem;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public <C> C buildController(Class<C> type) {
        if (type == this.getClass()) {
            // don't reinstantiate this class, use the existing instance
            return (C) this;
        }
        try {
            // if the controller class has a constructor that takes a XPmanFX parameter, use it
            Constructor<C> constructor = type.getConstructor(XPmanFX.class);
            return constructor.newInstance(this);
        } catch (NoSuchMethodException _) {
            // otherwise use no-arg constructor
            return type.getConstructor().newInstance();
        }
    }

    @FXML
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
        WindowPositionHelper.initialize(getConfig(), this::saveConfig);
        sidebarController.selectedSectionProperty().addListener((_, _, section) ->
                Optional.ofNullable(section).ifPresent(this::showSection));
        sidebarController.select(Section.HOME);
        XPManPrefs prefs = getConfig();
        if (prefs.getLastXPlanePath() != null) {
            Platform.runLater(() -> openXPlane(Paths.get(prefs.getLastXPlanePath()).toFile()));
        } else {
            Platform.runLater(this::open);
        }
    }

    /**
     * Selects the given section in the sidebar (which displays it in the content area).
     */
    public void navigateTo(Section section) {
        sidebarController.select(section);
    }

    private void showSection(Section section) {
        Node content = sectionCache.computeIfAbsent(section, this::loadSectionContent);
        if (contentArea.getChildren().isEmpty() || contentArea.getChildren().get(0) != content) {
            contentArea.getChildren().setAll(content);
        }
    }

    /** The controller of a displayed section, or null when the section was never displayed. */
    public Object getSectionController(Section section) {
        return sectionControllers.get(section);
    }

    @SneakyThrows
    private Node loadSectionContent(Section section) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(section.getContentFxml()));
        loader.setControllerFactory(this::buildController);
        Node content = loader.load();
        sectionControllers.put(section, loader.getController());
        return content;
    }

    /**
     * Registers Alt+1..5 and Shortcut+1..5 accelerators for direct section navigation.
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

    private void updateRecent() {
        List<? extends MenuItem> menuItems = getConfig().getRecentPaths().stream()
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
        stage.setScene(scene);

        installSectionAccelerators(scene);

        stage.setMinWidth(900);
        stage.setMinHeight(600);

        restoreWindowPosition(stage);
    }

    @Override
    public void saveConfig() {
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
    private void toggleDebugLogging() {
        Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        boolean enabled = debugLoggingItem.isSelected();
        root.setLevel(enabled ? Level.DEBUG : Level.INFO);
        log.info("Debug logging {}", enabled ? "enabled" : "disabled");
    }

    @FXML
    private void exportLogs() {
        Path logDir = Logs.logDir();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export XPman logs");
        chooser.setInitialFileName("xpman-logs-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP archives", "*.zip"));
        if (Files.isDirectory(logDir)) {
            chooser.setInitialDirectory(logDir.toFile());
        }
        File destination = chooser.showSaveDialog(primaryStage);
        if (destination == null) {
            return;
        }
        try {
            Logs.zip(logDir, destination.toPath());
            log.info("Exported logs to {}", destination);
        } catch (IOException e) {
            log.error("Failed to export logs", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not export XPman logs: " + e.getMessage());
            alert.setHeaderText("Log export failed");
            alert.initOwner(primaryStage);
            alert.showAndWait();
        }
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
        settings(SettingsCategory.TOOLS);
    }

    @FXML
    @SneakyThrows
    public void settings() {
        settings(Optional.ofNullable(getConfig().getSettingsCategory()).orElse(SettingsCategory.GENERAL));
    }

    @SneakyThrows
    public void settings(SettingsCategory category) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings/settings.fxml"));
        loader.setControllerFactory(this::buildController);
        Pane pane = loader.load();
        loader.<SettingsController>getController().select(category);
        Stage stage = new Stage();
        stage.setTitle("Settings");
        Scene scene = new Scene(pane);
        scene.getStylesheets().add(getClass().getResource("/css/xpman.css").toExternalForm());
        stage.setScene(scene);
        stage.initOwner(primaryStage);
        stage.initModality(Modality.APPLICATION_MODAL);
        applyWindowPosition(stage, getConfig().getSettingsPosition());
        stage.setOnHidden(_ -> {
            getConfig().setSettingsPosition(new JfxAppPrefs.WindowPosition(
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            saveConfig();
        });
        stage.showAndWait();
    }

    /** Menu item representing a recently opened X-Plane installation. */
    private class RecentMenuItem extends MenuItem {
        public RecentMenuItem(String folder) {
            super(folder);
            setOnAction(event -> Platform.runLater(() -> openXPlane(new File(folder))));
        }
    }
}
