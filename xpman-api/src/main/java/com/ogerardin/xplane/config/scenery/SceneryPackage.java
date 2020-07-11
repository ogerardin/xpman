package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.util.FileUtils;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Data
public class SceneryPackage {

    public static final String EARTH_NAV_DATA = "Earth nav data";

    @NonNull
    final Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    @SneakyThrows
    private int countTiles() {
        return FileUtils.findFiles(getEarthNavDataFolder(), path -> path.getFileName().toString().endsWith(".dsf")).size();
    }

    private Integer rank = null;

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
