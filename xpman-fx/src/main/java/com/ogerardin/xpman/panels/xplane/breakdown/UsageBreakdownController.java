package com.ogerardin.xpman.panels.xplane.breakdown;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xpman.XPmanFX;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.SneakyThrows;
import org.controlsfx.control.SegmentedBar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static com.ogerardin.xpman.panels.xplane.breakdown.UsageCategory.OTHER;

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
                .forEach(Platform::runLater);

        // schedule size computation
        AsyncHelper.runAsync(() -> computeSegments(xPlane));
    }

    public void initialize() {
        breakdown.setSegmentViewFactory(CategorySegmentView::new);
        breakdown.setInfoNodeFactory(SizeInfoNode::new);
    }

    @SneakyThrows
    private void computeSegments(XPlane xPlane) {
        // Phase 1: pre-compute folder sizes for categories that have a pathResolver
        Map<UsageCategory, Long> folderSizes = computeFolderSizes(xPlane);

        // Phase 2: compute all category results uniformly (fully polymorphic)
        Map<UsageCategory, UsageCategory.CategoryResult> results = new LinkedHashMap<>();
        for (UsageCategory category : UsageCategory.values()) {
            results.put(category, category.getSizeComputer().apply(xPlane, folderSizes));
        }

        // Phase 3: apply all results on FX thread in one pass
        Platform.runLater(() -> results.forEach((category, result) -> {
            CategorySegment seg = segmentFor(category);
            seg.setFolderPaths(result.folderPaths());
            seg.setValue((double) result.size());
        }));
    }

    @SneakyThrows
    private Map<UsageCategory, Long> computeFolderSizes(XPlane xPlane) {
        Map<UsageCategory, Long> sizes = new EnumMap<>(UsageCategory.class);
        for (UsageCategory category : UsageCategory.values()) {
            Function<XPlane, Path> resolver = category.getPathResolver();
            if (resolver == null) continue;
            Path folder = resolver.apply(xPlane);
            sizes.put(category, Files.exists(folder) ? FileUtils.getFolderSize(folder) : 0);
        }
        return sizes;
    }

    private CategorySegment segmentFor(UsageCategory category) {
        return breakdown.getSegments().stream()
                .filter(segment -> segment.getCategory() == category)
                .findFirst()
                .orElseThrow();
    }
}
