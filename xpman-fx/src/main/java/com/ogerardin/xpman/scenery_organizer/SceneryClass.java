package com.ogerardin.xpman.scenery_organizer;

import com.ogerardin.xplane.scenery.SceneryPackage;

public interface SceneryClass {

    String getName();

    boolean matches(SceneryPackage sceneryPackage);

}
