package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SegmentedBar.Segment;

import java.nio.file.Path;
import java.util.List;

/**
 * A specialized {@link Segment} that represents a {@link UsageCategory}.
 */
@Getter
@Slf4j
class CategorySegment extends Segment {

    private final UsageCategory category;

    @Setter
    private List<Path> folderPaths = List.of();

    private final BooleanProperty computing = new SimpleBooleanProperty(false);
    public final BooleanProperty computingProperty() {
        return computing;
    }

    public CategorySegment(UsageCategory category, double value) {
        super(value);
        this.category = category;
        setText(category.getText());
        valueProperty().addListener((_, _, _) -> computing.setValue(false));
    }

    public CategorySegment(UsageCategory category) {
        this(category, 0);
    }


}
