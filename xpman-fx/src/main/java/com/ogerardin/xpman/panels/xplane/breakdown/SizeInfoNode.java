package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.geometry.Insets;
import javafx.scene.control.Label;

import java.text.DecimalFormat;

/**
 * The view for the info node (tool tip) of a {@link CategorySegment}.
 * Displays the segment's text and a human-readable rendition of the segment's value interpreted a number of bytes.
 */
class SizeInfoNode extends Label {

    public static final DecimalFormat FORMAT = new DecimalFormat("#,##0.#");

    public SizeInfoNode(CategorySegment segment) {
        this(String.format("%s %s", segment.getText(), humanReadbleSize(segment.getValue())));
    }

    private SizeInfoNode(String text) {
        super(text);
        setPadding(new Insets(4));
        setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
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
