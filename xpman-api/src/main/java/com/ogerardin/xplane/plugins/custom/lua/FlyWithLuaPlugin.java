package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.install.types.PluginInstallableType;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
@Slf4j
public class FlyWithLuaPlugin extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = 
        "https://forums.x-plane.org/index.php?/files/file/82888-flywithlua-ng-next-generation-plus-edition-for-x-plane-12-win-lin-mac/";

    @Getter(lazy = true)
    private final List<FlyWithLuaScript> scripts = loadScripts();

    public FlyWithLuaPlugin(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "FlyWithLuaPlugin", "Lua scripting plugin for X-Plane", XPLANEORG_URL);
        require(isFlyWithLua(xplFile));
    }

    private boolean isFlyWithLua(Path xplFile) {
        // Check if the base folder (above platform-specific folder) is named "FlyWithLuaPlugin"
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder.getFileName().toString().equalsIgnoreCase("FlyWithLuaPlugin");
    }

    private List<FlyWithLuaScript> loadScripts() {
        Path scriptsFolder = getBaseFolder().resolve("Scripts");
        if (!scriptsFolder.toFile().exists()) {
            log.debug("FlyWithLuaPlugin Scripts folder not found: {}", scriptsFolder);
            return List.of();
        }
        
        try {
            List<Path> luaFiles = FileUtils.findFiles(scriptsFolder, path -> 
                path.getFileName().toString().endsWith(".lua")
            );
            
            log.debug("Found {} Lua scripts in {}", luaFiles.size(), scriptsFolder);
            
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
        } catch (java.io.IOException e) {
            log.warn("Failed to load FlyWithLuaPlugin scripts from {}", scriptsFolder, e);
            return List.of();
        }
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
    public String getTrashWarningDetails() {
        return " All FlyWithLuaPlugin scripts will also be deleted.";
    }

    /**
     * Installable type for FlyWithLuaPlugin plugin.
     * Recognizes archives with FlyWithLuaPlugin's characteristic folder structure
     * (FlyWithLuaPlugin folder containing Scripts subfolder and .xpl files).
     * Extends PluginInstallableType to have higher priority in type detection.
     */
    @SuppressWarnings("unused")
    public static class InstallableType extends PluginInstallableType {

        @Override
        public boolean recognizes(Archive archive) {
            // Check for FlyWithLuaPlugin folder structure:
            // - A folder named "FlyWithLuaPlugin" (case-insensitive)
            // - That contains a "Scripts" subfolder
            // - And contains .xpl files (in platform subfolder like 64/)

            boolean hasFlyWithLuaFolder = archive.getPaths().stream()
                .anyMatch(path -> {
                    String name = path.getFileName().toString();
                    return name.equalsIgnoreCase("FlyWithLuaPlugin") && path.getNameCount() >= 1;
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

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return CheckHasSingleRootFolder.INSTANCE.inspectable(archive).inspect();
        }

        @Override
        public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
            // FlyWithLuaPlugin installs as a regular plugin
            try {
                xPlane.getPluginManager().install(archive, progress);
            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }
    }
}
