package com.ogerardin.xpman.panels.xplane;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.xplane.breakdown.Segment;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentInfoNode;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentType;
import com.ogerardin.xpman.panels.xplane.breakdown.SegmentView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import lombok.SneakyThrows;
import org.controlsfx.control.SegmentedBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Label releaseUpdate;
    @FXML
    private Label betaUpdate;
    @FXML
    private SegmentedBar<Segment> breakdown;

    public XPlaneController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((observable, oldValue, xPlane) -> {
            this.xPlane = xPlane;
            updateDisplay(xPlane);
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> checkUpdates(xPlane));
        });
    }

    public void initialize() {
        breakdown.setSegmentViewFactory(SegmentView::new);
        breakdown.setInfoNodeFactory(SegmentInfoNode::new);
    }

    private void checkUpdates(XPlane xPlane) {
        final String currentVersion = xPlane.getVersion();
        if (currentVersion != null) {
            String latestFinal = UpdateInformation.getLatestFinal();
            boolean hasReleaseUpdate = latestFinal.compareToIgnoreCase(currentVersion) > 0;
            if (hasReleaseUpdate) {
                releaseUpdate.setText("Release version " + latestFinal + " is available. Run the X-Plane Installer to update.");
            }
            releaseUpdate.setVisible(hasReleaseUpdate);

            String latestBeta = UpdateInformation.getLatestBeta();
            boolean hasBetaUpdate = ! latestBeta.equals(latestFinal) && latestBeta.compareToIgnoreCase(currentVersion) > 0;
            if (hasBetaUpdate) {
                betaUpdate.setText("Beta version " + latestBeta + " is available. Run the X-Plane Installer to update.");
            }
            betaUpdate.setVisible(hasBetaUpdate);
        }
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

        Executors.newSingleThreadExecutor().submit(() -> computeSegments(xPlane));
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
