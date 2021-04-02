package com.ogerardin.xplane.navdata;

import java.util.Collections;
import java.util.List;

/**
 * A navigation data node as part of a hierarchy.
 */
public interface NavDataItem {

    String getName();

    default List<? extends NavDataItem> getChildren() {
        return Collections.emptyList();
    }

    Boolean getExists();

}
