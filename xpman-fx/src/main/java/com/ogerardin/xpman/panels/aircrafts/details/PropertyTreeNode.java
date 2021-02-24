package com.ogerardin.xpman.panels.aircrafts.details;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class PropertyTreeNode implements PropertyTreeItem{

    private final String name;

    private final List<PropertyTreeItem> children = new ArrayList<>();

    public PropertyTreeItem addChild(PropertyTreeItem item) {
        children.add(item);
        return item;
    }
}
