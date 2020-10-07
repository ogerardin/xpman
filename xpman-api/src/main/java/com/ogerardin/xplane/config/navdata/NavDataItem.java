package com.ogerardin.xplane.config.navdata;

import java.util.Collections;
import java.util.List;

public interface NavDataItem {

    String getName();

    default List<? extends NavDataItem> getChildren() {
        return Collections.emptyList();
    }

    Boolean getExists();

}
