package com.ogerardin.xpman.panels.home;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.shell.Section;
import com.ogerardin.xpman.tools.UiToolUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
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
@Slf4j
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

    public HomeController(XPmanFX mainController) {
        this.mainController = mainController;
        mainController.xPlaneProperty().addListener((__, ___, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            AsyncHelper.runAsync(() -> checkUpdates(xPlane));
        });
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
        trackCount(xPlane, XPlane::getAircraftManager, aircraftCount);
        trackCount(xPlane, XPlane::getSceneryManager, sceneryCount);
        trackCount(xPlane, XPlane::getNavDataManager, navDataCount);
        trackCount(xPlane, XPlane::getPluginManager, pluginsCount);
    }

    /**
     * Registers a listener on the given manager that updates the target label with the item count
     * whenever the manager loads, and triggers an initial load.
     */
    private <T> void trackCount(XPlane xPlane, Function<XPlane, Manager<T>> managerGetter, Label countLabel) {
        Manager<T> manager = managerGetter.apply(xPlane);
        manager.registerListener((ManagerEvent<T> event) -> {
            switch (event.getType()) {
                case LOADING -> Platform.runLater(() -> countLabel.setText("…"));
                case LOADED -> Platform.runLater(() ->
                        countLabel.setText(String.valueOf(event.getItems().size())));
                default -> {
                }
            }
        });
        manager.reload();
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
        Platforms.getCurrent().reveal(xPlane.getXPlaneExecutable());
    }

    @FXML
    public void startXPlane() {
        Platforms.getCurrent().startApp(xPlane.getXPlaneExecutable());
    }

    @FXML
    private void showLog() {
        Platforms.getCurrent().openFile(xPlane.getLogPath());
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
