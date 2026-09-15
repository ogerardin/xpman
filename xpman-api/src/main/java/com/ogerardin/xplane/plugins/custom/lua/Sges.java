package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized FlyWithLua script for Simple Ground Equipment & Services (SGES).
 * Recognizes Simple_Ground_Equipment_and_Services.lua and provides custom deletion logic.
 */
@SuppressWarnings("unused")
@Slf4j
public class Sges extends FlyWithLuaScript {

    private static final String SCRIPT_FILENAME = "Simple_Ground_Equipment_and_Services.lua";
    private static final String DATA_FOLDER = "Simple_Ground_Equipment_and_Services";
    private static final String DOCS_FOLDER = "Simple_Ground_Equipment_and_Services_expanded_documentation";

    public Sges(XPlane xPlane, Path luaFile) throws InstantiationException {
        super(xPlane, luaFile);
        require(luaFile.getFileName().toString().equals(SCRIPT_FILENAME));
    }

    @Override
    public String getVersion() {
        Pattern pattern = Pattern.compile("version_text_SGES\\s*=\\s*\"([^\"]+)\"");
        try (Stream<String> lines = Files.lines(getLuaFile())) {
            return lines
                    .map(pattern::matcher)
                    .filter(Matcher::find)
                    .findFirst()
                    .map(m -> m.group(1))
                    .orElseGet(super::getVersion);
        } catch (IOException e) {
            log.error("Error while reading lua script file", e);
            return super.getVersion();
        }
    }

    @Override
    public void uninstall() throws IOException {
        super.uninstall();

        Path scriptsFolder = getLuaFile().getParent();

        Path dataFolder = scriptsFolder.resolve(DATA_FOLDER);
        if (Files.exists(dataFolder)) {
            log.debug("Deleting SGES data folder: {}", dataFolder);
            com.sun.jna.platform.FileUtils.getInstance().moveToTrash(dataFolder.toFile());
        }

        Path docsFolder = scriptsFolder.resolve(DOCS_FOLDER);
        if (Files.exists(docsFolder)) {
            log.debug("Deleting SGES documentation folder: {}", docsFolder);
            com.sun.jna.platform.FileUtils.getInstance().moveToTrash(docsFolder.toFile());
        }
    }

    @Override
    public Map<String, Path> getManuals() {
        Map<String, Path> manuals = new HashMap<>();
        Path scriptFolder = getLuaFile().getParent();
        try (Stream<Path> paths = Files.list(scriptFolder)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> {
                     String name = p.getFileName().toString();
                     return name.toLowerCase().endsWith(".pdf") && name.startsWith("SGES");
                 })
                 .forEach(p -> manuals.put(p.getFileName().toString(), p));
        } catch (IOException e) {
            log.debug("Failed to scan for manuals in {}", scriptFolder, e);
        }
        return manuals;
    }

    /**
     * Installable type for SGES.
     * Recognizes archives containing Simple_Ground_Equipment_and_Services.lua.
     */
    @SuppressWarnings("unused")
    public static class SgesInstallableType extends FlyWithLuaScriptInstallableType {

        @Override
        public String description() {
            return "Simple Ground Equipment & Services";
        }

        @Override
        public boolean recognizes(Archive archive) {
            return archive.getPaths().stream()
                    .anyMatch(path -> path.getFileName().toString().equals(SCRIPT_FILENAME));
        }

        @Override
        public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
            try {
                deleteOldFiles(xPlane);

                Path scriptsFolder = findScriptsFolder(xPlane);
                if (scriptsFolder == null) {
                    throw new InstallationException("FlyWithLua Scripts folder not found");
                }

                Path archiveScriptsFolder = findScriptsFolderInArchive(archive);
                if (archiveScriptsFolder != null) {
                    archive.extract(scriptsFolder, archiveScriptsFolder, progress);
                } else {
                    archive.extract(scriptsFolder, progress);
                }
                xPlane.getPluginManager().reload();

            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }

        private Path findScriptsFolderInArchive(Archive archive) {
            return archive.getPaths().stream()
                    .filter(path -> path.getFileName().toString().equals(SCRIPT_FILENAME))
                    .map(Path::getParent)
                    .findFirst()
                    .orElse(null);
        }

        private void deleteOldFiles(XPlane xPlane) throws IOException {
            Path scriptsFolder = findScriptsFolder(xPlane);
            if (scriptsFolder == null) {
                return;
            }

            Path luaFile = scriptsFolder.resolve(SCRIPT_FILENAME);
            Path dataFolder = scriptsFolder.resolve(DATA_FOLDER);
            Path docsFolder = scriptsFolder.resolve(DOCS_FOLDER);

            if (Files.exists(luaFile)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(luaFile.toFile());
            }
            if (Files.exists(dataFolder)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(dataFolder.toFile());
            }
            if (Files.exists(docsFolder)) {
                com.sun.jna.platform.FileUtils.getInstance().moveToTrash(docsFolder.toFile());
            }
        }

    }
}
