package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;

/** Fallback {@link SceneryClass} for sceneries that match no other class. */
public enum OtherSceneryClass implements SceneryClass {

    INSTANCE;

    @Override
    public String getName() {
        return "Other";
    }

    @Override
    public boolean matches(SceneryPackage sceneryPackage) {
        return true;
    }
}
