package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.NonNull;

import java.nio.file.Path;

public class GlobalAirportsSceneryPackage extends SceneryPackage {

    public GlobalAirportsSceneryPackage(@NonNull Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(folder.getFileName().toString().equals(TokenSceneryPackIniItem.GLOBAL_AIRPORTS_FOLDER));
    }
}
