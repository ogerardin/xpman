package com.ogerardin.xpman.panels.xplane.breakdown;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xpman.XPmanFX;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.controlsfx.control.SegmentedBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.ogerardin.xpman.panels.xplane.breakdown.UsageCategory.*;

/**
 * Controller for the disk usage breakdown panel.
 * Displays one {@link CategorySegment} per {@link UsageCategory}, with the size of each category proportional to the
 * size of the corresponding folder in the X-Plane installation.
 */
public class UsageBreakdownController {

    @FXML private SegmentedBar<CategorySegment> breakdown;

    public UsageBreakdownController(XPmanFX mainController) {
        mainController.xPlaneProperty().addListener((__, ___, xPlane) -> updateDisplay(xPlane));
    }

    private void updateDisplay(XPlane xPlane) {
        //create segments with initial size of 1
        breakdown.getSegments().clear();
        Arrays.stream(UsageCategory.values())
                .map(category -> new CategorySegment(category, 1.0))
                .forEach(breakdown.getSegments()::add);

        // set all segments to "computing"
        breakdown.getSegments().stream()
                .<Runnable>map(segment -> () -> segment.computingProperty().setValue(true))
                .forEach(javafx.application.Platform::runLater);

        // schedule size computation
        AsyncHelper.runAsync(() -> computeSegments(xPlane));
    }

    public void initialize() {
        breakdown.setSegmentViewFactory(CategorySegmentView::new);
        breakdown.setInfoNodeFactory(SizeInfoNode::new);
    }

    @SneakyThrows
    private void computeSegments(XPlane xPlane) {
        //TODO use JavaFX property bindings?
        final long size = FileUtils.getFolderSize(xPlane.getBaseFolder());
        setSegment(OTHER, size);

        //TODO can this be done in parallel?
        computeSegment(AIRCRAFT, xPlane.getAircraftManager().getAircraftFolder());
        computeSegment(GLOBAL_SCENERY, xPlane.getPaths().globalScenery());
        computeSegment(CUSTOM_SCENERY, xPlane.getSceneryManager().getSceneryFolder());
        computeSegment(CUSTOM_SCENERY_DISABLED, xPlane.getSceneryManager().getDisabledSceneryFolder());
    }

    private long computeSegment(UsageCategory category, Path folder) throws IOException {
        final long folderSize = Files.exists(folder) ? FileUtils.getFolderSize(folder) : 0;
        Platform.runLater(() -> rebalance(OTHER, category, folderSize));
        return folderSize;
    }

    private void setSegment(UsageCategory category, long size) {
        breakdown.getSegments().stream()
                .filter(segment -> segment.getCategory() == category)
                .findFirst()
                .ifPresent(segment -> Platform.runLater(() -> segment.setValue((double) size)));
    }

    @Synchronized
    private void rebalance(UsageCategory fromCategory, UsageCategory toCategory, long folderSize) {
        for (CategorySegment segment : breakdown.getSegments()) {
            UsageCategory segmentCategory = segment.getCategory();
            if (segmentCategory == fromCategory) {
                segment.setValue(segment.getValue() - (double) folderSize);
            } else if (segmentCategory == toCategory) {
                segment.setValue(segment.getValue() + (double) folderSize);
            }
        }
    }

}
