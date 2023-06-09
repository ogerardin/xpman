package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;

enum OtherSceneryClass implements SceneryClass {

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
