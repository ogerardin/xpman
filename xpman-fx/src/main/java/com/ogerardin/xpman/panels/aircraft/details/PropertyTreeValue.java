package com.ogerardin.xpman.panels.aircraft.details;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/** A leaf node in the ACF property tree, holding a property name and its string value. */
@Data
@RequiredArgsConstructor
public class PropertyTreeValue implements PropertyTreeItem{

    private final String name;

    private final String value;

}
