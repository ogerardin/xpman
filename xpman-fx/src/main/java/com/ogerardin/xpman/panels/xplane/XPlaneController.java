package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.tools.UiToolUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for the first tab pane, which contains a summary of X-Plane installation.
 */
public class XPlaneController {

    private XPlane xPlane;

    @FXML
    private Button startXPlaneButton;
    @FXML
    private Hyperlink folder;
    @FXML
    private Label version;
    @FXML
    private Hyperlink log;
    @FXML
    private TextFlow releaseUpdateTextFlow;
    @FXML
    private TextFlow betaUpdateTextFlow;

    public XPlaneController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((__, ___, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            AsyncHelper.runAsync(() -> checkUpdates(xPlane));
        });
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
        boolean hasBetaUpdate = ! latestBeta.equals(latestFinal) && compareVersions(latestBeta, currentVersion) > 0;

        Platform.runLater(() -> {
            if (hasReleaseUpdate) {
                releaseUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Release", latestFinalReleaseInfo, xPlane));
            }
            releaseUpdateTextFlow.setVisible(hasReleaseUpdate);

            if (hasBetaUpdate) {
                betaUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Beta", latestBetaReleaseInfo, xPlane));
            }
            betaUpdateTextFlow.setVisible(hasBetaUpdate);
        });
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

    private static List<Node> buildUpdateMessage(String versionType, XPlaneReleaseInfo versionInfo, XPlane xPlane) {
        String version = versionInfo.version();
        List<Node> nodes = new ArrayList<>();
        Glyph warningGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_TRIANGLE) {{
            setStyle("-fx-text-fill: orange;");
        }};
        nodes.add(warningGlyph);
        nodes.add(new Label(" " + versionType + " " + version + " is available. Run the"));
        nodes.add(new Hyperlink("X-Plane Installer") {{
            setOnAction(__ -> {
                ToolsManager toolsManager = xPlane.getToolsManager();
                Tool xPlaneInstaller = toolsManager.getTool("xplane-installer");
                UiToolUtil.runTool(xPlane, xPlaneInstaller);
            });
        }});
        nodes.add(new Label("to update."));
        if (versionInfo.releaseNotesUrl() != null) {
            nodes.add(new Label(" Read the"));
            nodes.add(new Hyperlink("Release notes") {{
                setOnAction(__ -> Platforms.getCurrent().openUrl(versionInfo.releaseNotesUrl()));
            }});
        }
        return nodes;
    }

    private void updateDisplay(XPlane xPlane) {
        //TODO use bindings instead
        version.setText(String.format("%s (%s)", xPlane.getVersion(), xPlane.getVariant().name()));
        folder.setText(xPlane.getBaseFolder().toString());
        log.setText(xPlane.getLogPath().toString());
        // disable "start" button if current platform different from X-Plane detected platform
        startXPlaneButton.setDisable(! xPlane.getVariant().getPlatform().isCurrent());
    }

    public void showFolder() {
        Platforms.getCurrent().reveal(xPlane.getXPlaneExecutable());
    }

    public void startXPlane() {
        Platforms.getCurrent().startApp(xPlane.getXPlaneExecutable());
    }

    @FXML
    private void showLog() {
        Platforms.getCurrent().openFile(xPlane.getLogPath());
    }

}
