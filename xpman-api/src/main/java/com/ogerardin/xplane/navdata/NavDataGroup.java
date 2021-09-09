package com.ogerardin.xplane.navdata;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * A node for grouping {@link NavDataItem}s
 */
@Data
@RequiredArgsConstructor
public class NavDataGroup implements NavDataItem {

    private final String name;

    @ToString.Exclude
    private final List<? extends NavDataItem> items;

    @Override
    public List<? extends NavDataItem> getChildren() {
        return items;
    }

    public NavDataGroup(String name, NavDataItem... items) {
        this(name, Arrays.asList(items));
    }

    @Override
    public Boolean getExists() {
        return null;
    }
}
