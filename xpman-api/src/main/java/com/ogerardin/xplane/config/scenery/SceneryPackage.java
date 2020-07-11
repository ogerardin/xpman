package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.util.FileUtils;
import lombok.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
public class SceneryPackage {

    public static final String EARTH_NAV_DATA = "Earth nav data";

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    private boolean enabled = false;

    private Integer rank = null;


    @SneakyThrows
    private int countTiles() {
        return FileUtils.findFiles(getEarthNavDataFolder(), path -> path.getFileName().toString().endsWith(".dsf")).size();
    }

    public String getName() {
        return folder.getFileName().toString();
    }

    @SuppressWarnings("unused")
    public boolean isAirport() {
        return Files.exists(getEarthNavDataFolder().resolve("apt.dat"));
    }

    public Path getEarthNavDataFolder() {
        return folder.resolve(EARTH_NAV_DATA);
    }

    @SuppressWarnings("unused")
    public boolean isLibrary() {
        return Files.exists(folder.resolve("library.txt"));
    }

    public String getVersion() {
        return null;
    }



}
