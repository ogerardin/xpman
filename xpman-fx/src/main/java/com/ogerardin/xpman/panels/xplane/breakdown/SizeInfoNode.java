package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.geometry.Insets;
import javafx.scene.control.Label;

import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * The view for the info node (tool tip) of a {@link CategorySegment}.
 * Displays the segment's text, a human-readable rendition of the segment's value interpreted as a number of bytes,
 * and the relative X-Plane folder paths contributing to the category.
 */
class SizeInfoNode extends Label {

    public static final DecimalFormat FORMAT = new DecimalFormat("#,##0.#");

    public SizeInfoNode(CategorySegment segment) {
        this(buildTooltipText(segment));
    }

    private static String buildTooltipText(CategorySegment segment) {
        StringBuilder text = new StringBuilder(String.format("%s %s",
                segment.getText(), humanReadbleSize(segment.getValue())));
        for (Path p : segment.getFolderPaths()) {
            text.append("\n$X-Plane/").append(p);
        }
        return text.toString();
    }

    private SizeInfoNode(String text) {
        super(text);
        setPadding(new Insets(4));
        getStyleClass().add("segment-info");
    }

    private static String humanReadbleSize(double size) {
        if (size == 0 ) {
            return "0";
        }
        String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log10(size) / 3);
        double unitValue = 1 << (unitIndex * 10);
        return FORMAT.format(size / unitValue) + " " + units[unitIndex];
    }
}
