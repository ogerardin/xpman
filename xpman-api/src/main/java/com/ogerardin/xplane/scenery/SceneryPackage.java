package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import com.ogerardin.xplane.inspection.impl.ReferencedTexturesInspection;
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
public class SceneryPackage implements InspectionsProvider<SceneryPackage> {

    public static final String EARTH_NAV_DATA = "Earth nav data";

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    private boolean enabled = false;

    /** The rank of the scenery within scenery_packages.ini file (null if not listed) */
    private Integer rank = null;

    /** The number of geo-tiles (*.dsf files) contained in the sceery */
    @SneakyThrows
    private int countTiles() {
        return FileUtils.findFiles(getEarthNavDataFolder(), path -> path.getFileName().toString().endsWith(".dsf")).size();
    }

    public String getName() {
        return folder.getFileName().toString();
    }

    /** Whether the scenery contains an airport (file apt.dat) */
    @SuppressWarnings("unused")
    public boolean isAirport() {
        return Files.exists(getEarthNavDataFolder().resolve("apt.dat"));
    }

    public Path getEarthNavDataFolder() {
        return folder.resolve(EARTH_NAV_DATA);
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
    public Inspections<SceneryPackage> getInspections(XPlane xPlane) {
        return Inspections.of(new ReferencedTexturesInspection());
    }
}
