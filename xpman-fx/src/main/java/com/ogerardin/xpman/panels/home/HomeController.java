package com.ogerardin.xpman.panels.home;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard;
import com.ogerardin.xpman.shell.Section;
import com.ogerardin.xpman.tools.UiToolUtil;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller of the Home dashboard: summary of the X-Plane installation (version, folder, update
 * notifications, disk usage breakdown), an "Install anything..." entry point, and clickable tiles
 * with the item counts of each library section.
 */
public class HomeController {

    private final XPmanFX mainController;

    private XPlane xPlane;

    @FXML
    private Label titleLabel;
    @FXML
    private Hyperlink folder;
    @FXML
    private Hyperlink log;
    @FXML
    private Button startXPlaneButton;
    @FXML
    private TextFlow releaseUpdateTextFlow;
    @FXML
    private TextFlow betaUpdateTextFlow;
    @FXML
    private Label aircraftCount;
    @FXML
    private Label sceneryCount;
    @FXML
    private Label navDataCount;
    @FXML
    private Label pluginsCount;
    @FXML
    private Label aircraftUpdateBadge;
    @FXML
    private Label sceneryUpdateBadge;
    @FXML
    private Label pluginsUpdateBadge;
    @FXML
    private VBox updatesSection;
    @FXML
    private VBox updatesContent;

    private final IntegerProperty aircraftUpdateCount = new SimpleIntegerProperty(0);
    private final IntegerProperty sceneryUpdateCount = new SimpleIntegerProperty(0);
    private final IntegerProperty pluginsUpdateCount = new SimpleIntegerProperty(0);

    public HomeController(XPmanFX mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        mainController.xPlaneProperty().addListener((__, ___, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            AsyncHelper.runAsync(() -> checkUpdates(xPlane));
        });
        
        // Bind badge visibility and text to update counts
        aircraftUpdateBadge.textProperty().bind(aircraftUpdateCount.map(c -> c + " update" + (c.intValue() != 1 ? "s" : "")));
        aircraftUpdateBadge.visibleProperty().bind(aircraftUpdateCount.greaterThan(0));
        aircraftUpdateBadge.managedProperty().bind(aircraftUpdateCount.greaterThan(0));
        
        sceneryUpdateBadge.textProperty().bind(sceneryUpdateCount.map(c -> c + " update" + (c.intValue() != 1 ? "s" : "")));
        sceneryUpdateBadge.visibleProperty().bind(sceneryUpdateCount.greaterThan(0));
        sceneryUpdateBadge.managedProperty().bind(sceneryUpdateCount.greaterThan(0));
        
        pluginsUpdateBadge.textProperty().bind(pluginsUpdateCount.map(c -> c + " update" + (c.intValue() != 1 ? "s" : "")));
        pluginsUpdateBadge.visibleProperty().bind(pluginsUpdateCount.greaterThan(0));
        pluginsUpdateBadge.managedProperty().bind(pluginsUpdateCount.greaterThan(0));
    }

    private void updateDisplay(XPlane xPlane) {
        if (xPlane == null) {
            titleLabel.setText("No X-Plane installation selected");
            folder.setText("Select a folder...");
            log.setVisible(false);
            log.setManaged(false);
            startXPlaneButton.setDisable(true);
            return;
        }
        titleLabel.setText(String.format("X-Plane %s (%s)", xPlane.getVersion(), xPlane.getVariant().name()));
        folder.setText(xPlane.getBaseFolder().toString());
        log.setText(xPlane.getLogPath().toString());
        log.setVisible(true);
        log.setManaged(true);
        // disable "start" button if current platform different from X-Plane detected platform
        startXPlaneButton.setDisable(!xPlane.getVariant().getPlatform().isCurrent());
        trackCounts(xPlane);
    }

    private void trackCounts(XPlane xPlane) {
        trackCount(xPlane, XPlane::getAircraftManager, aircraftCount, this::checkAircraftUpdates);
        trackCount(xPlane, XPlane::getSceneryManager, sceneryCount, this::checkSceneryUpdates);
        trackCount(xPlane, XPlane::getNavDataManager, navDataCount, null);
        trackCount(xPlane, XPlane::getPluginManager, pluginsCount, this::checkPluginUpdates);
    }

    /**
     * Registers a listener on the given manager that updates the target label with the item count
     * whenever the manager loads, and triggers an initial load. Optionally runs an update check
     * after items are loaded.
     */
    private <T> void trackCount(XPlane xPlane, Function<XPlane, Manager<T>> managerGetter, Label countLabel, Runnable updateChecker) {
        Manager<T> manager = managerGetter.apply(xPlane);
        manager.registerListener((ManagerEvent<T> event) -> {
            switch (event.getType()) {
                case LOADING -> Platform.runLater(() -> countLabel.setText("…"));
                case LOADED -> Platform.runLater(() -> {
                        countLabel.setText(String.valueOf(event.getItems().size()));
                        if (updateChecker != null) {
                            AsyncHelper.runAsync(updateChecker);
                        }
                });
                default -> {
                }
            }
        });
        manager.reload();
    }

    private void checkAircraftUpdates() {
        List<Aircraft> aircraft = xPlane.getAircraftManager().getItems();
        if (aircraft == null) return;
        
        long count = aircraft.stream()
                .filter(SkunkcraftsUpdatable.class::isInstance)
                .map(SkunkcraftsUpdatable.class::cast)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                .count();
        
        Platform.runLater(() -> {
            aircraftUpdateCount.set((int) count);
            updateUpdatesSection();
        });
    }

    private void checkSceneryUpdates() {
        List<com.ogerardin.xplane.scenery.SceneryEntry> scenery = xPlane.getSceneryManager().getItems();
        if (scenery == null) return;
        
        long count = scenery.stream()
                .map(com.ogerardin.xplane.scenery.SceneryEntry::getSceneryPackage)
                .filter(java.util.Objects::nonNull)
                .filter(SkunkcraftsUpdatable.class::isInstance)
                .map(SkunkcraftsUpdatable.class::cast)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                .count();
        
        Platform.runLater(() -> {
            sceneryUpdateCount.set((int) count);
            updateUpdatesSection();
        });
    }

    private void checkPluginUpdates() {
        List<Plugin> plugins = xPlane.getPluginManager().getItems();
        if (plugins == null) return;
        
        long count = plugins.stream()
                .filter(SkunkcraftsUpdatable.class::isInstance)
                .map(SkunkcraftsUpdatable.class::cast)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                .count();
        
        Platform.runLater(() -> {
            pluginsUpdateCount.set((int) count);
            updateUpdatesSection();
        });
    }

    private void updateUpdatesSection() {
        // ponytail: rebuild from managers each time instead of accumulating state
        List<SkunkcraftsUpdatable> updates = new ArrayList<>();
        if (xPlane != null) {
            xPlane.getAircraftManager().getItems().stream()
                    .filter(SkunkcraftsUpdatable.class::isInstance)
                    .map(SkunkcraftsUpdatable.class::cast)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                    .forEach(updates::add);
            xPlane.getSceneryManager().getItems().stream()
                    .map(com.ogerardin.xplane.scenery.SceneryEntry::getSceneryPackage)
                    .filter(java.util.Objects::nonNull)
                    .filter(SkunkcraftsUpdatable.class::isInstance)
                    .map(SkunkcraftsUpdatable.class::cast)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                    .forEach(updates::add);
            xPlane.getPluginManager().getItems().stream()
                    .filter(SkunkcraftsUpdatable.class::isInstance)
                    .map(SkunkcraftsUpdatable.class::cast)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdatable)
                    .filter(SkunkcraftsUpdatable::isSkunkcraftsUpdateAvailable)
                    .forEach(updates::add);
        }
        
        boolean hasUpdates = !updates.isEmpty();
        updatesSection.setVisible(hasUpdates);
        updatesSection.setManaged(hasUpdates);
        
        if (!hasUpdates) return;
        
        updatesContent.getChildren().clear();
        
        // Group by category
        List<SkunkcraftsUpdatable> aircraftUpdates = updates.stream()
                .filter(u -> u instanceof Aircraft)
                .toList();
        List<SkunkcraftsUpdatable> sceneryUpdates = updates.stream()
                .filter(u -> u instanceof SceneryPackage)
                .toList();
        List<SkunkcraftsUpdatable> pluginUpdates = updates.stream()
                .filter(u -> u instanceof Plugin)
                .toList();
        
        if (!aircraftUpdates.isEmpty()) {
            updatesContent.getChildren().add(createCategorySection("Aircraft", aircraftUpdates));
        }
        if (!sceneryUpdates.isEmpty()) {
            updatesContent.getChildren().add(createCategorySection("Scenery", sceneryUpdates));
        }
        if (!pluginUpdates.isEmpty()) {
            updatesContent.getChildren().add(createCategorySection("Plugins", pluginUpdates));
        }
    }

    private VBox createCategorySection(String category, List<SkunkcraftsUpdatable> updates) {
        VBox section = new VBox(8);
        section.getStyleClass().add("updates-category");
        
        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add("updates-category-title");
        section.getChildren().add(categoryLabel);
        
        for (SkunkcraftsUpdatable updatable : updates) {
            section.getChildren().add(createUpdateItem(updatable));
        }
        
        return section;
    }

    private HBox createUpdateItem(SkunkcraftsUpdatable updatable) {
        HBox item = new HBox(12);
        item.getStyleClass().add("updates-item");
        item.setPadding(new Insets(8, 12, 8, 12));
        
        Label nameLabel = new Label(updatable.getName());
        nameLabel.getStyleClass().add("updates-item-name");
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
        
        String currentVersion = updatable.getVersion();
        String latestVersion = updatable.getSkunkcraftsLatestVersion();
        Label versionLabel = new Label(
                (currentVersion != null ? currentVersion : "?") + " → " + latestVersion
        );
        versionLabel.getStyleClass().add("updates-item-version");
        
        Button updateButton = new Button("Update");
        updateButton.getStyleClass().add("updates-item-button");
        updateButton.setOnAction(__ -> {
            new SkunkcraftsUpdateWizard(updatable).showAndWait();
            // reload the owning manager: re-reads versions from disk, re-triggers update checks
            if (updatable instanceof Aircraft) {
                xPlane.getAircraftManager().reload();
            } else if (updatable instanceof SceneryPackage) {
                xPlane.getSceneryManager().reload();
            } else if (updatable instanceof Plugin) {
                xPlane.getPluginManager().reload();
            }
        });
        
        item.getChildren().addAll(nameLabel, versionLabel, updateButton);
        return item;
    }

    private void checkUpdates(XPlane xPlane) {
        final String currentVersion = xPlane.getVersion();
        if (currentVersion == null) {
            return;
        }
        UpdateInformation updateInformation = xPlane.getMajorVersion().getUpdateInformation();
        XPlaneReleaseInfo latestFinalReleaseInfo = updateInformation.getLatestFinal();
        XPlaneReleaseInfo latestBetaReleaseInfo = updateInformation.getLatestBeta();
        String latestFinal = latestFinalReleaseInfo.version();
        String latestBeta = latestBetaReleaseInfo.version();

        boolean hasReleaseUpdate = compareVersions(latestFinal, currentVersion) > 0;
        boolean hasBetaUpdate = !latestBeta.equals(latestFinal) && compareVersions(latestBeta, currentVersion) > 0;

        Platform.runLater(() -> {
            if (hasReleaseUpdate) {
                releaseUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Release", latestFinalReleaseInfo, xPlane));
            }
            releaseUpdateTextFlow.setVisible(hasReleaseUpdate);
            releaseUpdateTextFlow.setManaged(hasReleaseUpdate);

            if (hasBetaUpdate) {
                betaUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Beta", latestBetaReleaseInfo, xPlane));
            }
            betaUpdateTextFlow.setVisible(hasBetaUpdate);
            betaUpdateTextFlow.setManaged(hasBetaUpdate);
        });
    }

    private static List<Node> buildUpdateMessage(String versionType, XPlaneReleaseInfo versionInfo, XPlane xPlane) {
        String version = versionInfo.version();
        List<Node> nodes = new ArrayList<>();
        FontIcon warningIcon = new FontIcon(Feather.ALERT_TRIANGLE);
        warningIcon.getStyleClass().add("warning-icon");
        nodes.add(warningIcon);
        nodes.add(new Label(" " + versionType + " " + version + " is available. Run the"));
        nodes.add(new Hyperlink("X-Plane Installer") {{
            setOnAction(__ -> {
                var toolsManager = xPlane.getToolsManager();
                var xPlaneInstaller = toolsManager.getTool("xplane-installer");
                UiToolUtil.runTool(xPlane, xPlaneInstaller);
            });
        }});
        nodes.add(new Label("to update."));
        if (versionInfo.releaseNotesUrl().isPresent()) {
            nodes.add(new Label(" Read the"));
            nodes.add(new Hyperlink("Release notes") {{
                setOnAction(__ -> Platforms.getCurrent().openUrl(versionInfo.releaseNotesUrl().get()));
            }});
            nodes.add(new Label("."));
        }
        return nodes;
    }

    /**
     * @return a negative integer, zero, or a positive integer as v1 is greater than, equal to, or less than v0.
     */
    private static int compareVersions(String v0, String v1) {
        return normalizeVersion(v0).compareToIgnoreCase(normalizeVersion(v1));
    }

    /**
     * Normalizes old-style versions like "12.04r3" to "12.0.4r3" for comparison
     */
    private static String normalizeVersion(String version) {
        Pattern pattern = Pattern.compile("(\\d\\d)\\.0((\\d)(.*))$");
        Matcher matcher = pattern.matcher(version);
        if (matcher.matches()) {
            return "%s.0.%s".formatted(matcher.group(1), matcher.group(2));
        }
        return version;
    }

    @FXML
    public void showFolder() {
        if (xPlane == null) {
            // no installation selected yet: the hyperlink acts as the folder chooser entry point
            mainController.open();
            return;
        }
        Platforms.getCurrent().reveal(xPlane.getXPlaneExecutable());
    }

    @FXML
    public void startXPlane() {
        Platforms.getCurrent().startApp(xPlane.getXPlaneExecutable());
    }

    @FXML
    private void showLog() {
        if (xPlane != null) {
            Platforms.getCurrent().openFile(xPlane.getLogPath());
        }
    }

    @FXML
    private void installAnything() {
        InstallWizard wizard = new InstallWizard(mainController.xPlaneProperty().getValue());
        wizard.showAndWait();
    }

    @FXML
    private void showAircraft() {
        mainController.navigateTo(Section.AIRCRAFT);
    }

    @FXML
    private void showScenery() {
        mainController.navigateTo(Section.SCENERY);
    }

    @FXML
    private void showNavData() {
        mainController.navigateTo(Section.NAV_DATA);
    }

    @FXML
    private void showPlugins() {
        mainController.navigateTo(Section.PLUGINS);
    }
}
