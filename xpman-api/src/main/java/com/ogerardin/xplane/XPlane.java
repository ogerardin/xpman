package com.ogerardin.xplane;

import com.ogerardin.xplane.aircraft.AircraftManager;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xplane.scenery.SceneryManager;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.tools.ToolsManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class XPlane {

    @EqualsAndHashCode.Include
    private final Path baseFolder;

    private final XPlaneVariant variant;

    private final XplanePaths paths = new XplanePaths();

    @Getter(lazy = true)
    private final String version = getVariant().getVersion(baseFolder);

    @Getter(lazy = true)
    private final XPlaneMajorVersion majorVersion = XPlaneMajorVersion.of(getVersion());

    @Getter(lazy = true)
    @ToString.Exclude
    private final AircraftManager aircraftManager = new AircraftManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final SceneryManager sceneryManager = new SceneryManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final PluginManager pluginManager = new PluginManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
    private final NavDataManager navDataManager = new NavDataManager(this);

    @Getter(lazy = true)
    @ToString.Exclude
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
        /** Global airports */
        public Path globalAirports() {
            // the path for global airports changed in X-Plane 12
            Path dir = (getMajorVersion() == XPlaneMajorVersion.XP11) ? customScenery() : globalScenery();
            return dir.resolve("Global Airports");
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
            return globalAirports().resolve(SceneryPackage.EARTH_NAV_DATA);
        }
    }
}

