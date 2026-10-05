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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
@Slf4j
public class FlyWithLua extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = 
        "https://forums.x-plane.org/index.php?/files/file/82888-flywithlua-ng-next-generation-plus-edition-for-x-plane-12-win-lin-mac/";

    public static final String FLY_WITH_LUA_PLUGIN = "FlyWithLua";

    @Getter(lazy = true)
    private final List<FlyWithLuaScript> scripts = loadScripts();

    public FlyWithLua(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, FLY_WITH_LUA_PLUGIN, "Lua scripting plugin for X-Plane", XPLANEORG_URL);
        require(isFlyWithLua(xplFile));
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
