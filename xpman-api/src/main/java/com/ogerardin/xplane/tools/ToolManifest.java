package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.laminar.UpdateInformation;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.sun.jna.Platform;
import com.sun.jna.platform.FileUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("ClassCanBeRecord")
@Data
@Builder
public class ToolManifest {

    @Getter(lazy = true)
    private static final List<ToolManifest> allManifests = loadAll();

    private final String name;
    private final String description;
    private final int platform;
    private final String exeName;
    private final Consumer<XPlane> installer;
    private final Function<Path, String> installedVersionGetter;
    @Builder.Default
    private final Consumer<InstalledTool> uninstaller = ToolManifest::defaultUninstaller;

    private final Supplier<String> availableVersionGetter;

    private static List<ToolManifest> loadAll() {
        return Arrays.asList(

                new ToolManifestBuilder()
                        .name("X-CSL updater for ALTITUDE client")
                        .platform(Platform.MAC)
                        .exeName("X-CSL-Updater.app")
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> ToolUtils. installFromDmg(xPlane, "https://csl.x-air.ru/download/src/409"))
                        .build(),

                new ToolManifestBuilder()
                        .name("X-CSL updater for IvAp client")
                        .platform(Platform.MAC)
                        .exeName("X-CSL-Updater.app")
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> ToolUtils. installFromDmg(xPlane, "https://csl.x-air.ru/download/src/362"))
                        .build(),

                new ToolManifestBuilder()
                        .name("OpenSceneryX installer 2.7.0")
                        .platform(Platform.MAC)
                        .exeName("OpenSceneryX Installer.app")
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> ToolUtils. installFromDmg(xPlane, "https://downloads.opensceneryx.com/OpenSceneryX-Installer-Mac-2.7.0.dmg"))
                        .build(),

                new ToolManifestBuilder()
                        .name("X-Plane installer")
                        .platform(Platform.MAC)
                        .exeName("X-Plane 11 Installer.app")
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> ToolUtils.installFromZip(xPlane, UpdateInformation.getLatestFinal()))
                        .build()
        );
    }

    @SneakyThrows
    private static void defaultUninstaller(InstalledTool t) {
        var fileUtils = FileUtils.getInstance();
        fileUtils.moveToTrash(t.getApp().toFile());
    }
}
