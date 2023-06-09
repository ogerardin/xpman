package com.ogerardin.xplane.scenery;

import lombok.NonNull;

import java.nio.file.Path;

public class GlobalAirportsSceneryPackage extends SceneryPackage {

    public static final String GLOBAL_AIRPORTS_MARKER =  "*GLOBAL_AIRPORTS*";

    public GlobalAirportsSceneryPackage(@NonNull Path folder) {
        super(folder);
    }
}
