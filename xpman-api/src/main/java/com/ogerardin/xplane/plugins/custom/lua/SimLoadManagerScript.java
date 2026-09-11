package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized FlyWithLuaPlugin script for SimLoad Manager.
 * Recognizes SimLoadManager.lua and provides custom deletion logic.
 */
@Slf4j
public class SimLoadManagerScript extends FlyWithLuaScript {
    
    public SimLoadManagerScript(XPlane xPlane, Path luaFile) throws InstantiationException {
        super(xPlane, luaFile);
        require(
            luaFile.getFileName().toString().equals("SimLoadManager.lua")
        );
    }
    
    @Override
    public void delete() throws IOException {
        // Delete the main script file
        super.delete();
        
        // Also delete the SLM-Data folder if it exists
        Path dataFolder = getLuaFile().getParent().resolve("SLM-Data");
        if (dataFolder.toFile().exists()) {
            log.debug("Deleting SimLoad Manager data folder: {}", dataFolder);
            com.sun.jna.platform.FileUtils.getInstance().moveToTrash(dataFolder.toFile());
        }
    }

    /**
     * Installable type for SimLoad Manager.
     * Recognizes archives containing SimLoadManager.lua and provides custom installation logic.
     */
    @SuppressWarnings("unused")
    public static class InstallableType extends FlyWithLuaScript.InstallableType {

        @Override
        public boolean recognizes(Archive archive) {
            return archive.getPaths().stream()
                .anyMatch(path -> path.getFileName().toString().equals("SimLoadManager.lua"));
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            InspectionResult result = super.preconditions(xPlane, archive);

            // Check for SGES script (warning, not error)
            boolean sgesInstalled = xPlane.getPluginManager().getPlugins().stream()
                .filter(p -> p instanceof FlyWithLuaPlugin)
                .map(FlyWithLuaPlugin.class::cast)
                .flatMap(fwl -> fwl.getScripts().stream())
                .anyMatch(s -> s.getName().contains("Simple Ground Equipment"));

            if (!sgesInstalled) {
                result = result.append(InspectionResult.of(
                    InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .message("Simple Ground Equipment & Services script not found")
                        .details("SimLoad Manager works best with SGES for visual ground services")
                        .build()
                ));
            }

            return result;
        }

        @Override
        public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
            try {
                // Delete old files first
                deleteOldFiles(xPlane);

                // Extract only SimLoadManager.lua and SLM-Data/
                Path scriptsFolder = findFlyWithLuaScriptsFolder(xPlane);
                if (scriptsFolder == null) {
                    throw new InstallationException("FlyWithLuaPlugin Scripts folder not found");
                }

                // Extract the entire archive to the scripts folder
                // The archive should contain SimLoadManager.lua and SLM-Data/
                archive.extract(scriptsFolder, progress);

                xPlane.getPluginManager().reload();

            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }

        private void deleteOldFiles(XPlane xPlane) throws IOException {
            Path scriptsFolder = findFlyWithLuaScriptsFolder(xPlane);
            if (scriptsFolder == null) {
                return;
            }

            Path luaFile = scriptsFolder.resolve("SimLoadManager.lua");
            Path dataFolder = scriptsFolder.resolve("SLM-Data");

            if (Files.exists(luaFile)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(luaFile.toFile());
            }
            if (Files.exists(dataFolder)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(dataFolder.toFile());
            }
        }

        private Path findFlyWithLuaScriptsFolder(XPlane xPlane) {
            List<Plugin> plugins = xPlane.getPluginManager().getPlugins();
            return plugins.stream()
                .filter(p -> p instanceof FlyWithLuaPlugin)
                .map(FlyWithLuaPlugin.class::cast)
                .findFirst()
                .map(fwl -> fwl.getBaseFolder().resolve("Scripts"))
                .orElse(null);
        }
    }
}
