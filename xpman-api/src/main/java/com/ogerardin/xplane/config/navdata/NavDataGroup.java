package com.ogerardin.xplane.config.navdata;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NavDataGroup implements NavDataItem {

    private final String name;

    private List<? extends NavDataItem> items = new ArrayList<>();

    @Override
    public List<? extends NavDataItem> getChildren() {
        return items;
    }
}
