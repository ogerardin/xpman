package com.ogerardin.xpman.panels.xplane.breakdown;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The categories of X-Plane usage.
 */
@RequiredArgsConstructor
@Getter
enum UsageCategory {
    AIRCRAFT("Aircraft", "segment-aircraft"),
    GLOBAL_SCENERY("Global scenery", "segment-global-scenery"),
    CUSTOM_SCENERY("Custom scenery", "segment-custom-scenery"),
    CUSTOM_SCENERY_DISABLED("Disabled scenery", "segment-disabled-scenery"),
    OTHER("Other", "segment-other");

    private final String text;
    private final String styleClass;
}
