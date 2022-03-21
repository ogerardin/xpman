package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.sun.jna.Platform;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("ClassCanBeRecord")
@Data
@Builder
public class ToolManifest {

    @Getter(lazy = true)
    private static final List<ToolManifest> allManifests = loadAll();

    private final String name;
    private final int platform;
    private final String exeName;
    private final Consumer<XPlane> installer;
    private final Function<Path, String> versionGetter;

    private static List<ToolManifest> loadAll() {
        return Arrays.asList(
                new ToolManifestBuilder()
                        .name("X-CSL updater")
                        .platform(Platform.MAC)
                        .exeName("X-CSL-Updater.app")
                        .versionGetter(MacPlatform::getMacVersion)
                        .installer(xPlane -> ToolUtils.installFromDmg(xPlane, "https://csl.x-air.ru/download/src/409"))
                        .build(),
                new ToolManifestBuilder()
                        .name("X-Plane installer")
                        .platform(Platform.MAC)
                        .exeName("X-Plane 11 Installer.app")
                        .versionGetter(MacPlatform::getMacVersion)
                        .installer(xPlane -> ToolUtils.installFromZip(xPlane, UpdateInformation.getLatestFinal()))
                        .build()
        );
    }

}
