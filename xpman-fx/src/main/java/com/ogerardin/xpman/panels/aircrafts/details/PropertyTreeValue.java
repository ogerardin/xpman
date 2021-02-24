package com.ogerardin.xpman.panels.aircrafts.details;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PropertyTreeValue implements PropertyTreeItem{

    private final String name;

    private final String value;

}
