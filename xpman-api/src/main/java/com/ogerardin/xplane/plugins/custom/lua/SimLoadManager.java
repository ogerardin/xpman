package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized FlyWithLua script for SimLoad Manager.
 * Recognizes SimLoadManager.lua and provides custom deletion logic.
 */
@Slf4j
public class SimLoadManager extends FlyWithLuaScript {

    public static final String SIM_LOAD_MANAGER_LUA = "SimLoadManager.lua";
    public static final String SLM_DATA_FOLDER = "SLM-Data";

    public SimLoadManager(XPlane xPlane, Path luaFile) throws InstantiationException {
        super(xPlane, luaFile);
        require(
            luaFile.getFileName().toString().equals(SIM_LOAD_MANAGER_LUA)
        );
    }
    
    @Override
    public void delete() throws IOException {
        // Delete the main script file
        super.delete();
        
        // Also delete the SLM-Data folder if it exists
        Path dataFolder = getLuaFile().getParent().resolve(SLM_DATA_FOLDER);
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
    public static class SimLoadManagerInstallableType extends FlyWithLuaScriptInstallableType {

        @Override
        public boolean recognizes(Archive archive) {
            return archive.getPaths().stream()
                .anyMatch(path -> path.getFileName().toString().equals(SIM_LOAD_MANAGER_LUA));
        }

        @Override
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            InspectionResult result = super.preconditions(xPlane, archive);

            // Check for SGES script (warning, not error)
            boolean sgesInstalled = xPlane.getPluginManager().getPlugins().stream()
                .filter(p -> p instanceof FlyWithLua)
                .map(FlyWithLua.class::cast)
                .flatMap(fwl -> fwl.getScripts().stream())
                .anyMatch(s -> s instanceof Sges);

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
                Path scriptsFolder = findScriptsFolder(xPlane);
                if (scriptsFolder == null) {
                    throw new InstallationException("FlyWithLua Scripts folder not found");
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
            Path scriptsFolder = findScriptsFolder(xPlane);
            if (scriptsFolder == null) {
                return;
            }

            Path luaFile = scriptsFolder.resolve(SIM_LOAD_MANAGER_LUA);
            Path dataFolder = scriptsFolder.resolve(SLM_DATA_FOLDER);

            if (Files.exists(luaFile)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(luaFile.toFile());
            }
            if (Files.exists(dataFolder)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(dataFolder.toFile());
            }
        }

    }
}
