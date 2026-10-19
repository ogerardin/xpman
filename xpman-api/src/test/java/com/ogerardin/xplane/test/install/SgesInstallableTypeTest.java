package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.custom.lua.Sges;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests that installing/uninstalling SGES handles the Skunkcrafts updater config file,
 * which the archive ships at its root (next to Scripts/) and the readme says must end up
 * in the FlyWithLua plugin folder — the addon base folder of the Skunkcrafts protocol.
 */
class SgesInstallableTypeTest {

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    private static final String CFG = "skunkcrafts_updater.cfg";
    private static final String BETA_CFG = "skunkcrafts_updater_beta.cfg";
    private static final String SCRIPT = "Simple_Ground_Equipment_and_Services.lua";
    private static final String CFG_CONTENT =
            "disabled|false\nzone|custom\nmodule|https://example.com/sges\nname|Simple Ground Equip. & Services\nversion|80.1\nlocked|false\n";
    private static final String BETA_CFG_CONTENT =
            "locked|false\nzone|custom\nmodule|https://example.com/sges-updates-beta\nname|Simple Ground Equip. & Services\n";

    @TempDir
    Path tempFolder;

    @Test
    void installPutsUpdaterCfgInFlyWithLuaFolder() throws Exception {
        Path zip = createSgesArchive();
        createFlyWithLuaPlugin();
        XPlane xPlane = new XPlane(tempFolder);

        new Sges.SgesInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path flyWithLuaFolder = flyWithLuaFolder();
        // the cfg must land in the FlyWithLua folder (the Skunkcrafts addon base folder)...
        assertThat(Files.readString(flyWithLuaFolder.resolve(CFG)), containsString("version|80.1"));
        assertThat(Files.exists(flyWithLuaFolder.resolve(BETA_CFG)), is(true));
        // ...while the Scripts subtree lands in the Scripts folder as before
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/" + SCRIPT)), is(true));
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/Simple_Ground_Equipment_and_Services")), is(true));
        // no wrapper folder leaked through
        assertThat(Files.exists(flyWithLuaFolder.resolve("SGES_80.1")), is(false));
    }

    @Test
    void installWithoutWrapperFolderStillExtractsScript() throws Exception {
        // flat archive: script directly at archive root, cfg next to it
        Path zip = Files.createTempFile(tempFolder, "sges-flat", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            entry(out, SCRIPT, "-- sges\n");
            entry(out, CFG, CFG_CONTENT);
        }
        createFlyWithLuaPlugin();
        XPlane xPlane = new XPlane(tempFolder);

        new Sges.SgesInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path flyWithLuaFolder = flyWithLuaFolder();
        assertThat(Files.readString(flyWithLuaFolder.resolve(CFG)), containsString("version|80.1"));
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/" + SCRIPT)), is(true));
        // the cfg must not be duplicated into Scripts/ by the flat-archive extraction
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/" + CFG)), is(false));
    }

    @Test
    void installWizardAnnouncesSkunkcraftsUpdatability() throws Exception {
        Path zip = createSgesArchive();
        createFlyWithLuaPlugin();
        XPlane xPlane = new XPlane(tempFolder);

        InspectionResult result = new Sges.SgesInstallableType().preconditions(xPlane, new ZipArchive(zip));

        assertThat(result.getMessages().stream()
                .anyMatch(msg -> msg.getSeverity() == Severity.INFO
                        && msg.getMessage().contains("Skunkcrafts Updater")), is(true));
    }

    @Test
    void uninstallRemovesUpdaterCfgFromFlyWithLuaFolder() throws Exception {
        Path luaFile = createInstalledScript();
        Files.writeString(flyWithLuaFolder().resolve(CFG), CFG_CONTENT);
        Files.writeString(flyWithLuaFolder().resolve(BETA_CFG), BETA_CFG_CONTENT);
        XPlane xPlane = new XPlane(tempFolder);

        List<Path> deleted = uninstallRecordingDeletions(xPlane, luaFile);

        // leaving the cfg behind would keep FlyWithLua permanently "update available"
        assertThat(deleted, containsInAnyOrder(
                luaFile, flyWithLuaFolder().resolve(CFG), flyWithLuaFolder().resolve(BETA_CFG)));
        assertThat(Files.exists(flyWithLuaFolder().resolve(CFG)), is(false));
        assertThat(Files.exists(flyWithLuaFolder().resolve(BETA_CFG)), is(false));
    }

    @Test
    void uninstallLeavesForeignUpdaterCfgInPlace() throws Exception {
        // the FlyWithLua folder is the addon base folder for every FlyWithLua addon: a config
        // belonging to another one is not ours to delete
        Path luaFile = createInstalledScript();
        Path foreignCfg = flyWithLuaFolder().resolve(CFG);
        Files.writeString(foreignCfg, "name|BetterPushback\nmodule|https://example.com/betterpushback\nversion|1.6.2\n");
        XPlane xPlane = new XPlane(tempFolder);

        List<Path> deleted = uninstallRecordingDeletions(xPlane, luaFile);

        assertThat(deleted, contains(luaFile));
        assertThat(Files.readString(foreignCfg), containsString("BetterPushback"));
    }

    // Fixture helpers

    private Path flyWithLuaFolder() {
        return tempFolder.resolve("Resources/plugins/FlyWithLua");
    }

    /** Creates the script file of an installed SGES (without going through install()). */
    private Path createInstalledScript() throws IOException {
        Path luaFile = flyWithLuaFolder().resolve("Scripts/" + SCRIPT);
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- sges\n");
        return luaFile;
    }

    /**
     * Uninstalls SGES with the trash call replaced by a recording delete, so the test neither
     * touches the real Trash nor depends on platform trash support.
     */
    private List<Path> uninstallRecordingDeletions(XPlane xPlane, Path luaFile)
            throws IOException, InstantiationException {
        List<Path> deleted = new ArrayList<>();
        new Sges(xPlane, luaFile) {
            @Override
            protected void moveToTrash(Path path) throws IOException {
                deleted.add(path);
                Files.deleteIfExists(path);
            }
        }.uninstall();
        return deleted;
    }

    private void createFlyWithLuaPlugin() throws IOException {
        Path xplFolder = flyWithLuaFolder().resolve("64");
        Files.createDirectories(xplFolder);
        Files.writeString(xplFolder.resolve("FlyWithLua.xpl"), "");
    }

    private Path createSgesArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "sges", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            // layout mirrors SGES_80.1.7z: wrapper folder with Scripts/ and cfg files at its root
            entry(out, "SGES_80.1/Scripts/" + SCRIPT, "-- sges\n");
            entry(out, "SGES_80.1/Scripts/Simple_Ground_Equipment_and_Services/CONFIG_options.lua", "-- options\n");
            entry(out, "SGES_80.1/" + CFG, CFG_CONTENT);
            entry(out, "SGES_80.1/" + BETA_CFG, BETA_CFG_CONTENT);
        }
        return zip;
    }

    private static void entry(ZipOutputStream out, String name, String content) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(content.getBytes());
        out.closeEntry();
    }
}
