package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.inspections.MissingSystemLibrariesInspection;
import com.ogerardin.xplane.install.types.PluginInstallableType;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialization of {@link XPlaneOrgPlugin} for the FlyWithLua plugin — a Lua scripting engine for X-Plane.
 * <p>
 * FlyWithLua uses a non-standard folder layout: the XPL resides under a platform-specific subfolder
 * (e.g. {@code 64/}) within a parent folder named "FlyWithLua", which also contains a {@code Scripts/}
 * directory. This class detects that layout and exposes the Lua scripts as {@link FlyWithLuaScript} instances.
 * <p>
 * FlyWithLua ships in three editions — Legacy (XP9–11), NG (XP11), NG+ (XP12) — distinguished by the
 * {@code PLUGIN_NAME} string compiled into the {@code .xpl} binary. The plugin's display name in the
 * panel reflects the installed edition.
 * <p>
 * On Linux, installation requires shared libraries {@code libglut} and {@code libopenal};
 * these are declared as preconditions by {@link FlyWithLuaInstallableType} and checked via
 * {@link MissingSystemLibrariesInspection}.
 *
 * @see FlyWithLuaScript
 * @see FlyWithLuaInstallableType
 */
@SuppressWarnings("unused")
@Slf4j
public class FlyWithLua extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = 
        "https://forums.x-plane.org/index.php?/files/file/82888-flywithlua-ng-next-generation-plus-edition-for-x-plane-12-win-lin-mac/";

    public static final String FLY_WITH_LUA_PLUGIN = "FlyWithLua";

    /** The three editions of FlyWithLua, identified by the {@code PLUGIN_NAME} string in the binary. */
    public enum Flavor {
        LEGACY("FlyWithLua"),
        NG("FlyWithLua NG"),
        NG_PLUS("FlyWithLua NG+");

        private final String label;

        Flavor(String label) { this.label = label; }

        public String label() { return label; }

        /**
         * Scans the binary content for the {@code PLUGIN_NAME} marker. Order matters: NG+ contains NG,
         * so check NG+ first. Unknown or empty content returns LEGACY (old label, no false claims).
         */
        public static Flavor of(String binaryContent) {
            if (binaryContent.contains("FlyWithLua NG+")) return NG_PLUS;
            if (binaryContent.contains("FlyWithLua NG")) return NG;
            return LEGACY;
        }
    }

    @Getter(lazy = true)
    private final List<FlyWithLuaScript> scripts = loadScripts();

    public FlyWithLua(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, detectFlavor(xplFile).label(), "Lua scripting plugin for X-Plane", XPLANEORG_URL);
        require(isFlyWithLua(xplFile));
    }

    /** Reads the .xpl binary and returns the detected flavor. Defaults to LEGACY on any read failure. */
    private static Flavor detectFlavor(Path xplFile) {
        try {
            return Flavor.of(new String(Files.readAllBytes(xplFile)));
        } catch (Exception e) {
            log.warn("Failed to read FlyWithLua .xpl for flavor detection: {}", xplFile, e);
            return Flavor.LEGACY;
        }
    }

    private boolean isFlyWithLua(Path xplFile) {
        // Check if the base folder (above platform-specific folder) is named "FlyWithLua"
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder.getFileName().toString().equalsIgnoreCase(FLY_WITH_LUA_PLUGIN);
    }

    private List<FlyWithLuaScript> loadScripts() {
        if (!isEnabled()) {
            log.debug("FlyWithLua is disabled; hiding scripts");
            return List.of();
        }
        Path scriptsFolder = getBaseFolder().resolve("Scripts");
        List<Path> folders = FlyWithLuaScript.scriptFolders(scriptsFolder);
        List<Path> luaFiles = new ArrayList<>();
        for (Path folder : folders) {
            if (!Files.isDirectory(folder)) continue;
            try (Stream<Path> stream = Files.list(folder)) {
                stream.filter(p -> p.getFileName().toString().endsWith(".lua"))
                      .forEach(luaFiles::add);
            } catch (java.io.IOException e) {
                log.warn("Failed to list FlyWithLua scripts in {}", folder, e);
            }
        }
        
        log.debug("Found {} Lua scripts across {}", luaFiles.size(), folders);
        
        return luaFiles.stream()
            .map(f -> {
                try {
                    return getBestSubclassInstance(FlyWithLuaScript.class, getXPlane(), f);
                } catch (InstantiationException e) {
                    log.warn("Failed to instantiate script for {}", f, e);
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Override
    protected boolean supportsSkunkcrafts() {
        // the updater cfg in this folder, if any, belongs to a hosted script (e.g. SGES), not to
        // the plugin: FlyWithLua itself ships from X-Plane.org and has no Skunkcrafts module
        return false;
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
            super.getLinks(),
            Maps.mapOf(
                "GitHub Repository", Urls.url("https://github.com/X-Friese/FlyWithLua")
            )
        );
    }

    @Override
    public String getUninstallWarningDetails() {
        return " All FlyWithLua scripts will also be deleted.";
    }

    /**
     * Installable type for FlyWithLua plugin.
     * Recognizes archives with FlyWithLua's characteristic folder structure
     * (FlyWithLua folder containing Scripts subfolder and .xpl files).
     * Extends PluginInstallableType to have higher priority in type detection.
     */
    @SuppressWarnings("unused")
    public static class FlyWithLuaInstallableType extends PluginInstallableType {

        @Override
        public String description() {
            return "FlyWithLua plugin";
        }

        @Override
        public String description(Archive archive) {
            return detectFlavorFromArchive(archive).label() + " plugin";
        }

        private static Flavor detectFlavorFromArchive(Archive archive) {
            return archive.getPaths().stream()
                    .filter(path -> path.getFileName().toString().endsWith(".xpl"))
                    .findFirst()
                    .map(xpl -> {
                        try {
                            return Flavor.of(archive.getAsText(xpl));
                        } catch (IOException e) {
                            return Flavor.LEGACY;
                        }
                    })
                    .orElse(Flavor.LEGACY);
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return super.preconditions(xPlane, archive).append(
                new MissingSystemLibrariesInspection(List.of(
                    new MissingSystemLibrariesInspection.SystemLibraryRequirement(Platforms.LINUX, "libglut.so",
                            "sudo apt-get install -y freeglut3 (if freeglut3 is unavailable, try freeglut3-dev)"),
                    new MissingSystemLibrariesInspection.SystemLibraryRequirement(Platforms.LINUX, "libopenal.so",
                            "sudo apt-get install -y libopenal1")
                )).inspect(archive));
        }

        @Override
        public boolean recognizes(Archive archive) {
            // Check for FlyWithLua folder structure:
            // - A folder named "FlyWithLua" (case-insensitive)
            // - That contains a "Scripts" subfolder
            // - And contains .xpl files (in platform subfolder like 64/)

            boolean hasFlyWithLuaFolder = archive.getPaths().stream()
                .anyMatch(path -> {
                    String name = path.getFileName().toString();
                    return name.equalsIgnoreCase(FLY_WITH_LUA_PLUGIN) && path.getNameCount() >= 1;
                });

            boolean hasScriptsFolder = archive.getPaths().stream()
                .anyMatch(path -> {
                    String name = path.getFileName().toString();
                    return name.equalsIgnoreCase("Scripts") &&
                           path.toString().toLowerCase().contains("flywithlua");
                });

            boolean hasXplFiles = archive.getPaths().stream()
                .anyMatch(path -> path.getFileName().toString().endsWith(".xpl"));

            return hasFlyWithLuaFolder && hasScriptsFolder && hasXplFiles;
        }

    }
}
