package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.diag.CheckResult;
import com.ogerardin.xplane.diag.Checkable;
import com.ogerardin.xplane.util.FileUtils;
import lombok.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class SceneryPackage implements Checkable {

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


    @Override
    public List<CheckResult> check(XPlaneInstance xPlaneInstance) {
        //TODO
        return Collections.emptyList();
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }
}
