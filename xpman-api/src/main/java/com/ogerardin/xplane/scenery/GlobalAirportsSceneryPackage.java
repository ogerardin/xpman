package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.NonNull;

import java.nio.file.Path;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/** The Global Airports scenery (the *GLOBAL_AIRPORTS* token's on-disk counterpart). */
public class GlobalAirportsSceneryPackage extends SceneryPackage {

    /** The name of the Global Airports folder, resolved according to the X-Plane version. */
    public static final String GLOBAL_AIRPORTS_FOLDER = "Global Airports";

    public GlobalAirportsSceneryPackage(@NonNull Path folder) throws InstantiationException {
        super(folder);
        require(folder.getFileName().toString().equals(GLOBAL_AIRPORTS_FOLDER));
    }
}
