package com.ogerardin.xpman.panels.xplane.breakdown;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SegmentedBar;

@Getter
@Slf4j
public class Segment extends SegmentedBar.Segment {
    private final SegmentType type;
    public Segment(SegmentType type, double value) {
        super(value);
        this.type = type;
        setText(type.getText());
    }

}
