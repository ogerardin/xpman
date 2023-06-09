package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;

/**
 * In X-Plane 12, Global Airports is a separate scenery class:
 * <ul><li> Resides in "Global Scenery" folder (instead of "Custom Scenery")</li>
 * <li> Referenced by *GLOBAL_AIRPORTS* in scenery_packs.ini</li></ul>
 */
enum GlobalAirportsSceneryClass implements SceneryClass {

    INSTANCE;

    @Override
    public String getName() {
        return "Global Airports";
    }

    @Override
    public boolean matches(SceneryPackage sceneryPackage) {
        return sceneryPackage.isLibrary();
    }
}
