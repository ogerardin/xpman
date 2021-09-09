package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SegmentedBar;

@Getter
@Slf4j
public class Segment extends SegmentedBar.Segment {

    private final SegmentType type;

    private final BooleanProperty computing = new SimpleBooleanProperty(false);
    public final BooleanProperty computingProperty() {
        return computing;
    }

    {
        // reset the computing to false whenever the value is changed
        valueProperty().addListener((observable, oldValue, newValue) -> computing.setValue(false));
    }

    public Segment(SegmentType type, double value) {
        super(value);
        this.type = type;
        setText(type.getText());
    }

    public Segment(SegmentType type) {
        this(type, 0);
    }






}
