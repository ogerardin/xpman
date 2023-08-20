package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.MissingReferencedTexturesInspection;
import com.ogerardin.xplane.util.FileUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Data
@Slf4j
public class SceneryPackage implements Inspectable {

    public static final String EARTH_NAV_DATA = "Earth nav data";
    public static final String OBJECTS = "Objects";

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    @Getter(lazy = true)
    private final int objCount = countObj();

    private boolean enabled = false;

    /** The rank of the scenery within scenery_packages.ini file (null if not listed) */
    private Integer rank = null;

    /** The number of geo-tiles (*.dsf files) contained in the sceery */
    @SneakyThrows
    private int countTiles() {
        return FileUtils.countFilesBySuffix(getEarthNavDataFolder(), ".dsf");
    }

    @SneakyThrows
    private int countObj() {
        return FileUtils.countFilesBySuffix(folder, ".obj");
    }

    public String getName() {
        return folder.getFileName().toString();
    }

    /** Whether the scenery contains an airport (file apt.dat) */
    @SuppressWarnings("unused")
    public boolean getHasAirport() {
        return Files.exists(getEarthNavDataFolder().resolve("apt.dat"));
    }

    public Path getEarthNavDataFolder() {
        return folder.resolve(EARTH_NAV_DATA);
    }

    public Path getObjectsFolder() {
        return folder.resolve(OBJECTS);
    }

    /** Whether the scenery is a library (containes a file library.txt) */
    @SuppressWarnings("unused")
    public boolean isLibrary() {
        return Files.exists(folder.resolve("library.txt"));
    }

    /** The scenery version. As there is no standard way for a scenery to declare its version, this method
     * should be overridden in specific scenery classes that provide a way to query the scenery version. */
    public String getVersion() {
        return null;
    }

    /** An optional URL that points to an icon for this scenery. */
    public URL getIconUrl() {
        return null;
    }


    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @Override
    public InspectionResult inspect() {
        return MissingReferencedTexturesInspection.INSTANCE.inspect(this);
    }
}
