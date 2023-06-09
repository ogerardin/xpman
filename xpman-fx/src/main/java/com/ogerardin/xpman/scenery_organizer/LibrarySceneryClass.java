package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;

enum LibrarySceneryClass implements SceneryClass {

    INSTANCE;

    @Override
    public String getName() {
        return "Library";
    }

    @Override
    public boolean matches(SceneryPackage sceneryPackage) {
        return sceneryPackage.isLibrary();
    }
}
