package com.ogerardin.xpman.panels.xplane.breakdown;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SegmentType {
    AIRCRAFTS("Aircrafts", "purple"),
    GLOBAL_SCENERY("Global scenery", "cadetblue"),
    CUSTOM_SCENERY("Custom scenery", "lightcoral"),
    CUSTOM_SCENERY_DISABLED("Disabled scenery", "orange"),
    OTHER("Other", "grey");

    private final String text;
    private final String color;
}
