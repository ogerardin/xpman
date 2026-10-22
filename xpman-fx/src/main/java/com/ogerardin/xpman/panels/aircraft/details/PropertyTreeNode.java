package com.ogerardin.xpman.panels.aircraft.details;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** An interior node in the ACF property tree, holding a name and a mutable list of child items. */
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
