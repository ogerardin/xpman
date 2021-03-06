package com.ogerardin.xplane;

import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xplane.scenery.SceneryManager;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
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
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final AircraftManager aircraftManager = new AircraftManager(this, paths.aircraft());

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final SceneryManager sceneryManager = new SceneryManager(this, paths.customScenery());

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final PluginManager pluginManager = new PluginManager(this, paths.plugins());

    @Getter(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final NavDataManager navDataManager = new NavDataManager(this);

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

    public Path getAppPath() {
        return getVariant().getAppPath(baseFolder);
    }


    /**
     * This is intended for tests, to test against a real X-Plane installation if one can be found.
     */
    @SneakyThrows
    public static Path getDefaultXPRootFolder() {
        // if we have a "X-Plane 11" resource directory, use it
        URL xp11rsc = XPlane.class.getResource("/X-Plane 11/");
        if (xp11rsc != null) {
            return Paths.get(xp11rsc.toURI());
        }
        // otherwise try a few common places
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path xplaneRoot = Stream.of(
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
    }
}

