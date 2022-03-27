package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.sun.jna.Platform;
import com.sun.jna.platform.FileUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.ogerardin.xplane.tools.ToolUtils.*;

@SuppressWarnings("ClassCanBeRecord")
@Data
@Builder
@Slf4j
public class ToolManifest {

    @Getter(lazy = true)
    private static final List<ToolManifest> allManifests = loadAll();

    private final String name;
    private final String description;
    private final int platform;
    private final Consumer<XPlane> installer;
    /** predicate to check if a given file matches this tool */
    @NonNull
    private final Predicate<Path> installChecker;
    private final Function<Path, String> installedVersionGetter;
    @Builder.Default
    private final Consumer<InstalledTool> uninstaller = ToolManifest::defaultUninstaller;

    private final Supplier<String> availableVersionGetter;

    private static List<ToolManifest> loadAll() {
        return Arrays.asList(

                new ToolManifestBuilder()
                        .name("X-CSL updater for ALTITUDE client (1.3.1)")
                        .description("X-CSL is library of online traffic models for users of the X-Plane flight simulator who fly in IVAO network. \n" +
                                "\n" +
                                "This version is for ALTITUDE Pilot Client version 1.9.20 or above only.")
                        .platform(Platform.MAC)
                        .installChecker(hasName("X-CSL-Updater.app").and(hasString("1.3.1")))
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> installFromDmg(xPlane, "https://csl.x-air.ru/download/src/409"))
                        .build(),

                new ToolManifestBuilder()
                        .name("X-CSL updater for IvAp client (1.2.0)")
                        .description("X-CSL is library of online traffic models for users of the X-Plane flight simulator who fly in IVAO network.\n" +
                                "\n" +
                                "This version is for X-IvAp Pilot Client version 0.4.x (and ALTITUDE Pilot Client version 1.9.19b and below)")
                        .platform(Platform.MAC)
                        .installChecker(hasName("X-CSL-Updater.app").and(hasString("1.2.0")))
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> installFromDmg(xPlane, "https://csl.x-air.ru/download/src/362"))
                        .build(),

                new ToolManifestBuilder()
                        .name("OpenSceneryX installer 2.7.0")
                        .description("OpenSceneryX is a collection of x-plane scenery objects, contributed by various members of the community free-of-charge. The library has become a comprehensive set of high-quality scenery elements for use by any scenery author, reducing the time taken to build a custom scenery package and reducing the load on X-Plane. OpenSceneryX is built as a proper x-plane library, which means that authors don't copy objects out of the package into their sceneries, instead they reference the objects directly from the library. So, if several authors have used the same church from the library, that object only needs to be loaded once by x-plane.")
                        .platform(Platform.MAC)
                        .installChecker(hasName("OpenSceneryX Installer.app"))
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> installFromDmg(xPlane, "https://downloads.opensceneryx.com/OpenSceneryX-Installer-Mac-2.7.0.dmg"))
                        .build(),

                new ToolManifestBuilder()
                        .name("X-Plane installer")
                        .description("Laminar Research's installer/updater for X-Plane 11.")
                        .platform(Platform.MAC)
                        .installChecker(hasName("X-Plane 11 Installer.app"))
                        .installedVersionGetter(MacPlatform::getMacAppVersion)
                        .installer(xPlane -> installFromZip(xPlane, "https://www.x-plane.com/update/installers11/X-Plane11InstallerMac.zip"))
                        .build()
        );
    }

    @SneakyThrows
    private static void defaultUninstaller(InstalledTool t) {
        var fileUtils = FileUtils.getInstance();
        fileUtils.moveToTrash(t.getApp().toFile());
    }
}
