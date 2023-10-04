package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
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
        UpdateInformation updateInformation = new UpdateInformation(xPlane.getMajorVersion());
        String latestFinal = updateInformation.getLatestFinal();
        String latestBeta = updateInformation.getLatestBeta();
        boolean hasReleaseUpdate = latestFinal.compareToIgnoreCase(currentVersion) > 0;
        boolean hasBetaUpdate = ! latestBeta.equals(latestFinal) && latestBeta.compareToIgnoreCase(currentVersion) > 0;

        Platform.runLater(() -> {
            if (hasReleaseUpdate) {
                releaseUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Release", latestFinal, xPlane));
            }
            releaseUpdateTextFlow.setVisible(hasReleaseUpdate);

            if (hasBetaUpdate) {
                betaUpdateTextFlow.getChildren().setAll(buildUpdateMessage("Beta", latestBeta, xPlane));
            }
            betaUpdateTextFlow.setVisible(hasBetaUpdate);
        });
    }

    private static List<Node> buildUpdateMessage(String versionType, String version, XPlane xPlane) {
        List<Node> nodes = new ArrayList<>();
        Glyph warningGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_TRIANGLE) {{
            setStyle("-fx-text-fill: orange;");
        }};
        nodes.add(warningGlyph);
        nodes.add(new Label(" " + versionType + " " + version + " is available. Run the"));
        nodes.add(new Hyperlink("X-Plane Installer") {{
            setOnAction(event -> {
                ToolsManager toolsManager = xPlane.getToolsManager();
                Tool xPlaneInstaller = toolsManager.getTool("xplane-installer");
                UiToolUtil.runTool(xPlane, xPlaneInstaller);
            });
        }});
        nodes.add(new Label("to update."));
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
