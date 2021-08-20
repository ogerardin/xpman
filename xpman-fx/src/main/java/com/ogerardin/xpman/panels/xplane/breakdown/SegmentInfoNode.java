package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.geometry.Insets;
import javafx.scene.control.Label;

import java.text.DecimalFormat;

public class SegmentInfoNode extends Label {

    public SegmentInfoNode(Segment segment) {
        this(String.format("%s %s", segment.getText(), humanReadbleSize(segment.getValue())));
    }

    private SegmentInfoNode(String text) {
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
        return new DecimalFormat("#,##0.#").format(size / unitValue) + " " + units[unitIndex];
    }
}
