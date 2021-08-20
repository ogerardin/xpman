package com.ogerardin.xplane.navdata;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * A node for grouping {@link NavDataItem}s
 */
@Data
public class NavDataGroup implements NavDataItem {

    private final String name;

    private List<? extends NavDataItem> items = new ArrayList<>();

    @Override
    public List<? extends NavDataItem> getChildren() {
        return items;
    }

    @Override
    public Boolean getExists() {
        return null;
    }
}
