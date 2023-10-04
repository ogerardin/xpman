package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SegmentedBar.Segment;

/**
 * A specialized {@link Segment} that represents a {@link UsageCategory}.
 */
@Getter
@Slf4j
class CategorySegment extends Segment {

    private final UsageCategory category;

    private final BooleanProperty computing = new SimpleBooleanProperty(false);
    public final BooleanProperty computingProperty() {
        return computing;
    }

    {
        // reset the computing property to false whenever the value is changed
        valueProperty().addListener((__, ___, ____) -> computing.setValue(false));
    }

    public CategorySegment(UsageCategory category, double value) {
        super(value);
        this.category = category;
        setText(category.getText());
    }

    public CategorySegment(UsageCategory category) {
        this(category, 0);
    }






}
