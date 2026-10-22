package com.ogerardin.xpman.panels.aircraft.details;

import java.util.Collections;
import java.util.List;

/** Common interface for nodes in an ACF property tree, providing a name and optional children. */
public interface PropertyTreeItem {

    String getName();

    @SuppressWarnings("java:S1452") // Covariant child lists let implementations expose their concrete node type.
    default List<? extends PropertyTreeItem> getChildren() {
        return Collections.emptyList();
    }

}
