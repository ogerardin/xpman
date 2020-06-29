package com.ogerardin.xplane.config;

import com.ogerardin.xplane.config.aircrafts.AircraftManager;
import com.ogerardin.xplane.config.plugins.PluginManager;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

@Slf4j
@Data
public class XPlaneInstance {

    private final Path rootFolder;

    private final XPlaneVariant variant;

    @Getter(lazy = true)
    private final String version = getVariant().getVersion(rootFolder);

    @Getter(lazy = true)
    @ToString.Exclude
    private final AircraftManager aircraftManager = new AircraftManager(rootFolder.resolve("Aircraft"));

    @Getter(lazy = true)
    @ToString.Exclude
    private final PluginManager pluginManager = new PluginManager(rootFolder.resolve("Resources").resolve("plugins"));

    public XPlaneInstance(Path rootFolder) throws InvalidConfig {
        if (!Files.isDirectory(rootFolder)) {
            throw new InvalidConfig("Folder " + rootFolder + " does not exist");
        }
        this.variant = computeVariant(rootFolder);
        this.rootFolder = rootFolder;
    }

    private static XPlaneVariant computeVariant(Path rootFolder) throws InvalidConfig {
        return Arrays.stream(XPlaneVariant.values())
                .filter(v -> v.applies(rootFolder))
                .findAny()
                .orElseThrow(() ->
                    new InvalidConfig("No X-Plane application not found in " + rootFolder)
                );

    }

    public Path getExePath() {
        return getVariant().getExePath(rootFolder);
    }


    @SneakyThrows
    public static Path getDefaultXPRootFolder() {
        URL xp11rsc = XPlaneInstance.class.getResource("/X-Plane 11/");
        if (xp11rsc != null) {
            return Paths.get(xp11rsc.toURI());
        }

        Path userHome = Paths.get(System.getProperty("user.home"));
        Path xplaneRoot = Stream.of(
                userHome.resolve("Applications").resolve("X-Plane 11"),
                userHome.resolve("Desktop").resolve("X-Plane 11")
        )
                .filter(path -> Files.isDirectory(path))
                .findFirst()
                .orElseThrow(() -> new InvalidConfig("Failed to find an X-Plane root folder"));

        log.info("\n\nUsing X-Plane root folder '{}'\n", xplaneRoot);
        return xplaneRoot;
    }
}

