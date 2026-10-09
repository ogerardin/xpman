package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.MissingReferencedTexturesInspection;
import com.ogerardin.xplane.inspection.impl.MissingSceneryDataInspection;
import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.util.FileUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Data
@Slf4j
public class SceneryPackage implements Inspectable, Uninstallable, SkunkcraftsUpdatable {

    public static final String EARTH_NAV_DATA = "Earth nav data";
    public static final String OBJECTS = "Objects";

    @NonNull
    @Setter(AccessLevel.PACKAGE)
    private Path folder;

    @Getter(lazy = true)
    private final int tileCount = countTiles();

    @Getter(lazy = true)
    private final int objCount = countObj();

    /** Whether this package is enabled according to its ini entry; unlisted packages are enabled by default. */
    private boolean enabled = true;

    private boolean system = false;

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

    @Getter(lazy = true)
    private final SkunkcraftsConfig skunkcraftsConfig = SkunkcraftsUpdater.findConfig(getSkunkcraftsFolder());

    @Override
    public Path getSkunkcraftsFolder() {
        return folder;
    }

    @Override
    public boolean isSkunkcraftsUpdatable() {
        return isEnabled() && SkunkcraftsUpdatable.super.isSkunkcraftsUpdatable();
    }

    @Getter(lazy = true)
    private final String skunkcraftsLatestVersion = isSkunkcraftsUpdatable()
            ? SkunkcraftsUpdater.fetchRemoteVersion(getSkunkcraftsConfig().moduleUrl())
            : null;

    /** An optional URL that points to an icon for this scenery. */
    public URL getIconUrl() {
        return null;
    }


    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @Override
    public InspectionResult inspect() {
        return MissingSceneryDataInspection.INSTANCE
                .and(MissingReferencedTexturesInspection.INSTANCE)
                .inspect(this)
                .append(isEnabled()
                        ? UpdateAvailableInspection.INSTANCE.inspect(this)
                        : InspectionResult.empty());
    }

    @Override
    public void uninstall() throws IOException {
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());
    }
}
