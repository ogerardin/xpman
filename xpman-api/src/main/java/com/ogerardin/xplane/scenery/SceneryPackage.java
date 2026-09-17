package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.impl.MissingReferencedTexturesInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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

    private boolean enabled = false;

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

    public String getLatestVersion() {
        return getSkunkcraftsLatestVersion();
    }

    @Getter(lazy = true)
    private final SkunkcraftsConfig skunkcraftsConfig = SkunkcraftsUpdater.findConfig(folder);

    @Override
    public boolean isSkunkcraftsUpdatable() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && !cfg.disabled() && !cfg.locked() && cfg.moduleUrl() != null;
    }

    @Override
    public boolean isSkunkcraftsLocked() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && cfg.locked();
    }

    @Override
    public String getSkunkcraftsLatestVersion() {
        if (!isSkunkcraftsUpdatable()) return null;
        return SkunkcraftsUpdater.fetchRemoteVersion(getSkunkcraftsConfig().moduleUrl());
    }

    @Override
    public boolean isSkunkcraftsUpdateAvailable() {
        String latest = getSkunkcraftsLatestVersion();
        return latest != null && !Objects.equals(getVersion(), latest);
    }

    @Override
    public int getSkunkcraftsFilesToUpdateCount() {
        if (!isSkunkcraftsUpdatable()) {
            return 0;
        }
        return SkunkcraftsUpdater.computeFilesToUpdateCount(folder, getSkunkcraftsConfig());
    }

    @Override
    public void applySkunkcraftsUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException {
        if (!isSkunkcraftsUpdatable()) {
            throw new SkunkcraftsUpdateException("Scenery is not Skunkcrafts-updatable");
        }
        SkunkcraftsUpdater.applyUpdate(folder, getSkunkcraftsConfig(), progress);
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
        InspectionResult result = MissingReferencedTexturesInspection.INSTANCE.inspect(this);
        String latest = getLatestVersion();
        if (latest != null && !Objects.equals(getVersion(), latest)) {
            result = result.append(InspectionResult.of(
                    InspectionMessage.builder()
                            .severity(Severity.WARN)
                            .object(getName())
                            .message("Update available: " + latest)
                            .build()
            ));
        }
        return result;
    }

    @Override
    public void uninstall() throws IOException {
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());
    }
}
