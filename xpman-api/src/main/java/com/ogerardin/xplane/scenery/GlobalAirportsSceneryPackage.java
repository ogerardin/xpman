package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.NonNull;

import java.nio.file.Path;

public class GlobalAirportsSceneryPackage extends SceneryPackage {

    public static final String GLOBAL_AIRPORTS_MARKER =  "*GLOBAL_AIRPORTS*";

    public GlobalAirportsSceneryPackage(@NonNull Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(folder.getFileName().toString().equals("Global Airports"));
    }
}
