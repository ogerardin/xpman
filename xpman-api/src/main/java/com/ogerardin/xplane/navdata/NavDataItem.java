package com.ogerardin.xplane.navdata;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A navigation data node as part of a hierarchy.
 */
public interface NavDataItem {

    String getName();

    default Path getPath() {
        return null;
    }

    default String getDescription() {
        return null;
    }

    default String getAiracCycle() {
        return null;
    }
    default String getMetadata() {
        return null;
    }
    default String getBuild() {
        return null;
    }
    /**
     * The name the dataset gives itself in its header, for formats that carry one
     * (ARINC 424). Null for formats without a dataset name.
     */
    default String getDatasetName() {
        return null;
    }

    default List<? extends NavDataItem> getChildren() {
        return Collections.emptyList();
    }

    Boolean getExists();

}
