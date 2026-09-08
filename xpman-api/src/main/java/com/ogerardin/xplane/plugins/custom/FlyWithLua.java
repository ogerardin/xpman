package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class FlyWithLua extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = 
        "https://forums.x-plane.org/index.php?/files/file/82888-flywithlua-ng-next-generation-plus-edition-for-x-plane-12-win-lin-mac/";

    @Getter(lazy = true)
    private final List<FlyWithLuaScript> scripts = loadScripts();

    public FlyWithLua(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "FlyWithLua", "Lua scripting plugin for X-Plane", XPLANEORG_URL);
        IntrospectionHelper.require(isFlyWithLua(xplFile));
    }

    private boolean isFlyWithLua(Path xplFile) {
        // Check if the base folder (above platform-specific folder) is named "FlyWithLua"
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder.getFileName().toString().equalsIgnoreCase("FlyWithLua");
    }

    private List<FlyWithLuaScript> loadScripts() {
        Path scriptsFolder = getBaseFolder().resolve("Scripts");
        if (!scriptsFolder.toFile().exists()) {
            log.debug("FlyWithLua Scripts folder not found: {}", scriptsFolder);
            return List.of();
        }
        
        try {
            List<Path> luaFiles = FileUtils.findFiles(scriptsFolder, path -> 
                path.getFileName().toString().endsWith(".lua")
            );
            
            log.debug("Found {} Lua scripts in {}", luaFiles.size(), scriptsFolder);
            
            return luaFiles.stream()
                .map(f -> new FlyWithLuaScript(getXPlane(), f))
                .toList();
        } catch (java.io.IOException e) {
            log.warn("Failed to load FlyWithLua scripts from {}", scriptsFolder, e);
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
        return " All FlyWithLua scripts will also be deleted.";
    }
}
