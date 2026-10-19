package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.Sges;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @TempDir
    Path tempFolder;

    @Test
    void installPutsUpdaterCfgInFlyWithLuaFolder() throws Exception {
        Path zip = createSgesArchive();
        createFlyWithLuaPlugin();
        XPlane xPlane = new XPlane(tempFolder);

        new Sges.SgesInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path flyWithLuaFolder = tempFolder.resolve("Resources/plugins/FlyWithLua");
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

        Path flyWithLuaFolder = tempFolder.resolve("Resources/plugins/FlyWithLua");
        assertThat(Files.readString(flyWithLuaFolder.resolve(CFG)), containsString("version|80.1"));
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/" + SCRIPT)), is(true));
        // the cfg must not be duplicated into Scripts/ by the flat-archive extraction
        assertThat(Files.exists(flyWithLuaFolder.resolve("Scripts/" + CFG)), is(false));
    }

    @Test
    void uninstallRemovesUpdaterCfgFromFlyWithLuaFolder() throws Exception {
        // set up an installed SGES directly (independent of install() so this test cannot pass vacuously)
        createFlyWithLuaPlugin();
        Path flyWithLuaFolder = tempFolder.resolve("Resources/plugins/FlyWithLua");
        Path luaFile = flyWithLuaFolder.resolve("Scripts/" + SCRIPT);
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- sges\n");
        Files.writeString(flyWithLuaFolder.resolve(CFG), CFG_CONTENT);
        Files.writeString(flyWithLuaFolder.resolve(BETA_CFG), "locked|false\n");
        XPlane xPlane = new XPlane(tempFolder);

        new Sges(xPlane, luaFile).uninstall();

        // leaving the cfg behind would keep FlyWithLua permanently "update available"
        assertThat(Files.exists(flyWithLuaFolder.resolve(CFG)), is(false));
        assertThat(Files.exists(flyWithLuaFolder.resolve(BETA_CFG)), is(false));
    }

    // Fixture helpers

    private void createFlyWithLuaPlugin() throws IOException {
        Path xplFolder = tempFolder.resolve("Resources/plugins/FlyWithLua/64");
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
            entry(out, "SGES_80.1/" + BETA_CFG, "locked|false\n");
        }
        return zip;
    }

    private static void entry(ZipOutputStream out, String name, String content) throws IOException {
        out.putNextEntry(new ZipEntry(name));
        out.write(content.getBytes());
        out.closeEntry();
    }
}
