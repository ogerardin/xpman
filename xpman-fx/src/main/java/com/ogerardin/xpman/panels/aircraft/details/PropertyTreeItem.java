package com.ogerardin.xpman.panels.aircraft.details;

import java.util.Collections;
import java.util.List;

public interface PropertyTreeItem {

    String getName();

    default List<? extends PropertyTreeItem> getChildren() {
        return Collections.emptyList();
    }

}
