package com.ogerardin.xplane.plugins.custom.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.SkunkcraftsUpdatableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized FlyWithLua script for Simple Ground Equipment & Services (SGES).
 * Recognizes Simple_Ground_Equipment_and_Services.lua and provides custom deletion logic.
 *
 * <p>SGES ships a Skunkcrafts updater config next to its Scripts/ folder; it is installed into
 * the FlyWithLua folder (the addon base folder the protocol resolves updates against) and
 * removed again on uninstall. SGES is therefore the {@link SkunkcraftsUpdatable} addon — the
 * FlyWithLua plugin hosting it is not.
 */
@SuppressWarnings("unused")
@Slf4j
public class Sges extends FlyWithLuaScript implements SkunkcraftsUpdatable {

    private static final String SCRIPT_FILENAME = "Simple_Ground_Equipment_and_Services.lua";
    private static final String DATA_FOLDER = "Simple_Ground_Equipment_and_Services";
    private static final String DOCS_FOLDER = "Simple_Ground_Equipment_and_Services_expanded_documentation";
    // the Skunkcrafts protocol ships the updater cfg next to Scripts/; it belongs in the
    // FlyWithLua folder, which becomes the addon base folder for updates
    // The protocol has one cfg slot per addon base folder (skunkcrafts_updater.cfg); a second
    // Skunkcrafts-managed FlyWithLua script would overwrite the first — the identity guard
    // then migrates the pill to the surviving cfg's script. Upgrade when the protocol supports
    // per-addon cfg names or subfolders.
    private static final List<String> UPDATER_CFG_FILES = List.of("skunkcrafts_updater.cfg", "skunkcrafts_updater_beta.cfg");

    public Sges(XPlane xPlane, Path luaFile) throws InstantiationException {
        super(xPlane, luaFile);
        require(luaFile.getFileName().toString().equals(SCRIPT_FILENAME));
    }

    @Override
    public String getName() {
        return "Simple Ground Equipment & Services";
    }

    /** The updater cfg of the addon base folder, but only if it identifies itself as SGES. */
    @Getter(lazy = true)
    private final SkunkcraftsConfig skunkcraftsConfig = ownUpdaterCfg();

    @Override
    public Path getSkunkcraftsFolder() {
        // the cfg lives in the FlyWithLua folder: the addon base folder the whitelist paths resolve against
        return activeScriptsFolder().getParent();
    }

    @Override
    public boolean isSkunkcraftsUpdatable() {
        // gate on enabled like Plugin: applying an update while SGES sits in Scripts (disabled)/
        // would download the whitelist into the active Scripts folder, duplicating the script
        return isEnabled() && SkunkcraftsUpdatable.super.isSkunkcraftsUpdatable();
    }

    @Getter(lazy = true)
    private final String skunkcraftsLatestVersion = isSkunkcraftsUpdatable()
            ? SkunkcraftsUpdater.fetchRemoteVersion(getSkunkcraftsConfig().moduleUrl())
            : null;

    /** Content-truth availability of both Skunkcrafts channels, cached so the UI never pays the check on the FX thread. */
    @Getter(lazy = true)
    private final SkunkcraftsChannelStatus skunkcraftsChannelStatus = SkunkcraftsUpdater.channelStatus(this);

    /**
     * The beta updater cfg of the addon base folder, but only if it identifies itself as SGES.
     */
    @Override
    public SkunkcraftsConfig getSkunkcraftsBetaConfig() {
        SkunkcraftsConfig cfg = SkunkcraftsUpdater.findBetaConfig(getSkunkcraftsFolder());
        return cfg != null && identifiesAsSges(cfg) ? cfg : null;
    }

    /**
     * The updater cfg of the addon base folder, or null if there is none or if it belongs to
     * another FlyWithLua addon sharing that folder.
     */
    private SkunkcraftsConfig ownUpdaterCfg() {
        SkunkcraftsConfig cfg = SkunkcraftsUpdater.findConfig(getSkunkcraftsFolder());
        return cfg != null && identifiesAsSges(cfg) ? cfg : null;
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
            moveToTrash(dataFolder);
        }

        Path docsFolder = scriptsFolder.resolve(DOCS_FOLDER);
        if (Files.exists(docsFolder)) {
            log.debug("Deleting SGES documentation folder: {}", docsFolder);
            moveToTrash(docsFolder);
        }

        // Remove the updater cfg files from the FlyWithLua folder, otherwise the plugin would
        // stay Skunkcrafts-managed after SGES is gone. That folder is the addon base folder of
        // every FlyWithLua addon, so only delete a config that identifies itself as SGES.
        Path flyWithLuaFolder = activeScriptsFolder().getParent();
        for (String cfgName : UPDATER_CFG_FILES) {
            Path cfg = flyWithLuaFolder.resolve(cfgName);
            if (!Files.exists(cfg)) {
                continue;
            }
            if (isSgesConfig(cfg)) {
                log.debug("Deleting Skunkcrafts updater config: {}", cfg);
                moveToTrash(cfg);
            } else {
                log.warn("Leaving Skunkcrafts updater config {} in place: it does not look like SGES", cfg);
            }
        }
    }

    /**
     * True if the given updater config belongs to SGES, so that a config belonging to another
     * FlyWithLua addon sharing the same base folder is left alone.
     */
    private static boolean isSgesConfig(Path cfg) {
        try {
            return identifiesAsSges(SkunkcraftsConfig.parse(cfg));
        } catch (IOException e) {
            log.warn("Could not read Skunkcrafts config {}, leaving it in place", cfg, e);
            return false;
        }
    }

    /** Loose match on the cfg's identity, so a repo rename alone does not break ownership. */
    private static boolean identifiesAsSges(SkunkcraftsConfig config) {
        String identity = (config.name() + " " + config.moduleUrl()).toLowerCase();
        return identity.contains("sges") || identity.contains("ground equip");
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

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "Plugin page on X-Plane.org",
                Urls.url("https://forums.x-plane.org/files/file/62296-simple-ground-equipment-services-low-tech-services/")
        );
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
        public InspectionResult preconditions(XPlane xPlane, Archive archive) {
            return super.preconditions(xPlane, archive)
                    .append(SkunkcraftsUpdatableInspection.INSTANCE.inspect(archive));
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
                    // flat archive: keep the updater cfg files out of Scripts/, they go to the FlyWithLua folder
                    archive.extract(scriptsFolder, path -> !isUpdaterCfg(path.getFileName().toString()), progress);
                }
                extractUpdaterCfg(archive, scriptsFolder.getParent());
                xPlane.getPluginManager().reload();

            } catch (IOException e) {
                throw new InstallationException(e);
            }
        }

        private Path findScriptsFolderInArchive(Archive archive) {
            return archive.getPaths().stream()
                    .filter(path -> path.getFileName().toString().equals(SCRIPT_FILENAME))
                    .map(Path::getParent)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        /**
         * Writes the updater cfg files (shipped next to Scripts/ in the archive) into the FlyWithLua
         * folder, which is the addon base folder the Skunkcrafts protocol resolves updates against.
         */
        private void extractUpdaterCfg(Archive archive, Path flyWithLuaFolder) throws IOException {
            // shortcut: the cfg is copied as text (UTF-8), which is fine while SGES ships ASCII
            // pipe-delimited cfgs; switch to a byte-level copy if a non-UTF-8 cfg ever appears
            for (Path path : archive.getPaths()) {
                if (isUpdaterCfg(path.getFileName().toString())) {
                    Files.createDirectories(flyWithLuaFolder);
                    Files.writeString(flyWithLuaFolder.resolve(path.getFileName()), archive.getAsText(path));
                }
            }
        }

        private static boolean isUpdaterCfg(String fileName) {
            return UPDATER_CFG_FILES.contains(fileName);
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
