package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.tools.ToolsManager;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.xplane.breakdown.Segment;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentInfoNode;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentType;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import lombok.SneakyThrows;
import org.controlsfx.control.SegmentedBar;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @FXML
    private SegmentedBar<Segment> breakdown;

    private static final Glyph WARNING_GLYPH = new Glyph("FontAwesome", FontAwesome.Glyph.EXCLAMATION_TRIANGLE) {{
        setStyle("-fx-text-fill: orange;");
    }};

    public XPlaneController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((observable, oldValue, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            AsyncHelper.runAsync(() -> checkUpdates(xPlane));
        });
    }

    public void initialize() {
        breakdown.setSegmentViewFactory(SegmentView::new);
        breakdown.setInfoNodeFactory(SegmentInfoNode::new);
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
        nodes.add(WARNING_GLYPH);
        nodes.add(new Label(" " + versionType + " " + version + " is available. Run the"));
        nodes.add(new Hyperlink("X-Plane Installer") {{
            setOnAction(event -> {
                ToolsManager toolsManager = xPlane.getToolsManager();
                Tool xPlaneInstaller = toolsManager.getTool("xplane-installer");
                toolsManager.launchTool(xPlaneInstaller);
            });
        }});
        nodes.add(new Label("to update."));
        return nodes;
    }

    private void updateDisplay(XPlane xPlane) {
        version.setText(String.format("%s (%s)", xPlane.getVersion(), xPlane.getVariant().name()));
        folder.setText(xPlane.getBaseFolder().toString());
//        appPath.setText(xPlane.getAppPath().toString());
        log.setText(xPlane.getLogPath().toString());
        // disable "start" button if current platform different from X-Plane detected platform
        startXPlaneButton.setDisable(! xPlane.getVariant().getPlatform().isCurrent());

//        breakdown.getSegments().clear();
        breakdown.getSegments().setAll(
                new Segment(SegmentType.AIRCRAFTS, 1),
                new Segment(SegmentType.GLOBAL_SCENERY, 1),
                new Segment(SegmentType.CUSTOM_SCENERY, 1),
                new Segment(SegmentType.CUSTOM_SCENERY_DISABLED, 1),
                new Segment(SegmentType.OTHER, 1)
        );

        // set all segments to "computing"
        breakdown.getSegments().stream()
                .<Runnable>map(segment -> () -> segment.computingProperty().setValue(true))
                .forEach(javafx.application.Platform::runLater);

        AsyncHelper.runAsync(() -> computeSegments(xPlane));
    }

    @SneakyThrows
    private void computeSegments(XPlane xPlane) {

        final long size = FileUtils.getFolderSize(xPlane.getBaseFolder());

        javafx.application.Platform.runLater(
                () -> breakdown.getSegments().forEach(segment -> segment.setValue(size / 5.0))
        );

        long remainingSize = size;
        remainingSize -= computeSegment(SegmentType.AIRCRAFTS, xPlane.getAircraftManager().getAircraftFolder());
        remainingSize -= computeSegment(SegmentType.GLOBAL_SCENERY, xPlane.getPaths().globalScenery());
        remainingSize -= computeSegment(SegmentType.CUSTOM_SCENERY, xPlane.getSceneryManager().getSceneryFolder());
        remainingSize -= computeSegment(SegmentType.CUSTOM_SCENERY_DISABLED, xPlane.getSceneryManager().getDisabledSceneryFolder());
        setSegment(SegmentType.OTHER, remainingSize);
    }

    private long computeSegment(SegmentType type, Path folder) throws IOException {
        long folderSize = 0;
        if (Files.exists(folder)) {
            folderSize = FileUtils.getFolderSize(folder);
        }
        setSegment(type, folderSize);
        return folderSize;
    }

    private void setSegment(SegmentType type, long folderSize) {
        breakdown.getSegments().stream()
                .filter(segment -> segment.getType() == type)
                .<Runnable>map(segment -> () -> segment.setValue(folderSize))
                .forEach(javafx.application.Platform::runLater);
    }

    @SneakyThrows
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
