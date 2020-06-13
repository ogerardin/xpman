package com.ogerardin.xplane.config;

import com.ogerardin.xplane.config.aircrafts.AircraftManager;
import com.ogerardin.xplane.config.plugins.PluginManager;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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

}

