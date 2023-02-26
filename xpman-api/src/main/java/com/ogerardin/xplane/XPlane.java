package com.ogerardin.xplane;

import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xplane.scenery.SceneryManager;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.tools.ToolsManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Data
public class XPlane {

    private final Path baseFolder;

    private final XPlaneVariant variant;

    private final XplanePaths paths = new XplanePaths();

    @Getter(lazy = true)
    private final String version = getVariant().getVersion(baseFolder);

    @Getter(lazy = true)
    private final XPlaneMajorVersion majorVersion = XPlaneMajorVersion.of(getVersion());

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final AircraftManager aircraftManager = new AircraftManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final SceneryManager sceneryManager = new SceneryManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final PluginManager pluginManager = new PluginManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final NavDataManager navDataManager = new NavDataManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final ToolsManager toolsManager = new ToolsManager(this);

    public XPlane(Path baseFolder) throws InvalidConfig {
        if (!Files.isDirectory(baseFolder)) {
            throw new InvalidConfig("Folder " + baseFolder + " does not exist");
        }
        this.variant = computeVariant(baseFolder);
        this.baseFolder = baseFolder;
    }

    private static XPlaneVariant computeVariant(Path rootFolder) {
        return Arrays.stream(XPlaneVariant.values())
                .filter(v -> v.applies(rootFolder))
                .findFirst()
                .orElse(XPlaneVariant.UNKNOWN);
    }

    public Path getXPlaneExecutable() {
        return getVariant().getAppPath(baseFolder);
    }

    @Getter(lazy = true)
    private static final Path defaultXPRootFolder = computeDefaultXPRootFolder();

    /**
     * This is intended for tests, to test against a real X-Plane installation if one can be found.
     */
    @SneakyThrows
    private static Path computeDefaultXPRootFolder() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path xplaneRoot = Stream.of(
                userHome.resolve("Applications").resolve("X-Plane 12"),
                userHome.resolve("Desktop").resolve("X-Plane 12"),
                userHome.resolve("Applications").resolve("X-Plane 11"),
                userHome.resolve("Desktop").resolve("X-Plane 11")
        )
                .filter(Files::isDirectory)
                .findFirst()
                .orElseThrow(() -> new InvalidConfig("Failed to find an X-Plane root folder"));

        log.info("\n\nUsing X-Plane root folder '{}'\n", xplaneRoot);
        return xplaneRoot;
    }

    public Path getLogPath() {
        return getBaseFolder().resolve("Log.txt");
    }

    public class XplanePaths {
        public Path aircraft() {
            return getBaseFolder().resolve("Aircraft");
        }
        public Path customData() {
            return getBaseFolder().resolve("Custom Data");
        }
        public Path customScenery() {
            return getBaseFolder().resolve("Custom Scenery");
        }
        /** Custom disabled scenary folder (not X-Plane standard) */
        public Path disabledCustomScenery() {
            return customScenery().resolveSibling(customScenery().getFileName() + " (disabled)");
        }
        public Path globalScenery() {
            return getBaseFolder().resolve("Global Scenery");
        }
        public Path resources() {
            return getBaseFolder().resolve("Resources");
        }
        public Path defaultData() {
            return resources().resolve("default data");
        }
        public Path plugins() {
            return resources().resolve("plugins");
        }
        /** Custom tools folder (not X-Plane standard) */
        public Path tools() { return resources().resolve("tools"); }
        public Path handPlacedLocalizers() {
            // the path for hand-placed localizers changed in X-Plane 12
            Path dir = (getMajorVersion() == XPlaneMajorVersion.XP11) ? customScenery() : globalScenery();
            return dir.resolve(SceneryPackage.EARTH_NAV_DATA);
        }
    }
}

