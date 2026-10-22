package com.ogerardin.xpman.panels.aircraft.details;

import lombok.Data;
import lombok.experimental.Delegate;

/** UI model wrapping a {@link PropertyTreeItem} for display in the ACF tree table, extracting the value string for leaf nodes. */
@Data
public class UiProperty {

    @Delegate
    private final PropertyTreeItem item;

    public String getValue() {
        return (item instanceof PropertyTreeValue ptv) ? ptv.getValue() : null;
    }

}
