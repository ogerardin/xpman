package com.ogerardin.xpman.panels.aircrafts.details;

import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class UiProperty {

    @Delegate
    private final PropertyTreeItem item;

    public String getValue() {
        return item instanceof PropertyTreeValue ? ((PropertyTreeValue) item).getValue() : null;
    }

}
