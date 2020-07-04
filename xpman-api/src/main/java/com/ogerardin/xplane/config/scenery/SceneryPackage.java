package com.ogerardin.xplane.config.scenery;

import lombok.Data;
import lombok.NonNull;

import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class SceneryPackage {

    public static final String EARTH_NAV_DATA = "Earth nav data";

    @NonNull
    final Path folder;

    private Integer rank = null;

    public String getName() {
        return folder.getFileName().toString();
    }

    public boolean isAirport() {
        return Files.exists(getEarthNavDataFolder().resolve("apt.dat"));
    }

    public Path getEarthNavDataFolder() {
        return folder.resolve(EARTH_NAV_DATA);
    }

    public boolean isLibrary() {
        return Files.exists(folder.resolve("library.txt"));
    }

    public String getVersion() {
        return null;
    }


}
