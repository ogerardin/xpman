package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.plugins.custom.lua.Sges;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.test.skunkcrafts.SkunkcraftsServerFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests that SGES — the FlyWithLua script — is the Skunkcrafts-updatable addon, not the
 * FlyWithLua plugin hosting it: the updater cfg installed in the FlyWithLua folder belongs
 * to SGES, so the update must be offered on the script row, and the plugin must not claim it.
 * Availability is content truth: the remote whitelist's CRCs against the local files.
 */
class SgesSkunkcraftsTest {

    private static final String SCRIPT = "Simple_Ground_Equipment_and_Services.lua";
    private static final String CFG = "skunkcrafts_updater.cfg";

    @TempDir
    Path tempDir;

    @Test
    void sgesClaimsItsUpdaterCfgAndReportsTheRemoteVersion() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            String moduleUrl = server.baseUrl() + "sges";
            Path luaFile = installedSges("Scripts", sgesCfg(moduleUrl), null);
            server.serve("sges/skunkcrafts_updater.cfg", remoteCfg("81.2", moduleUrl));
            // the whitelist's CRC differs from the local script: content behind
            server.serve("sges/skunkcrafts_updater_whitelist.txt", whitelist(crcOf("newer script content")));

            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.isSkunkcraftsUpdatable(), is(true));
            // the FlyWithLua folder is the addon base folder the whitelist paths resolve against
            assertThat(sges.getSkunkcraftsFolder(), is(luaFile.getParent().getParent()));
            // the installed version is the cfg's, not the lua header's
            assertThat(sges.getInstalledVersion(), is("80.1"));
            assertThat(sges.getSkunkcraftsLatestVersion(), is("81.2"));
            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(true));
            assertThat(sges.isSkunkcraftsBetaUpdateAvailable(), is(false));
        }
    }

    @Test
    void upToDateSgesReportsNoUpdateEvenThoughLabelsDiffer() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            String moduleUrl = server.baseUrl() + "sges";
            Path luaFile = installedSges("Scripts", sgesCfg(moduleUrl), null);
            server.serve("sges/skunkcrafts_updater.cfg", remoteCfg("81.2", moduleUrl));
            // local matches remote content, only the version label differs (the openSAM case)
            server.serve("sges/skunkcrafts_updater_whitelist.txt", whitelist(SkunkcraftsUpdater.computeCRC32(luaFile)));

            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(false));
            assertThat(sges.isAnySkunkcraftsUpdateAvailable(), is(false));
        }
    }

    @Test
    void sgesBetaChannelIsOfferedWhenBetaContentDiffers() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            String stableUrl = server.baseUrl() + "sges";
            String betaUrl = server.baseUrl() + "beta";
            Path luaFile = installedSges("Scripts", sgesCfg(stableUrl), betaCfg(betaUrl));
            server.serve("sges/skunkcrafts_updater.cfg", remoteCfg("81.2", stableUrl));
            server.serve("sges/skunkcrafts_updater_whitelist.txt", whitelist(SkunkcraftsUpdater.computeCRC32(luaFile)));
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("81.3-beta", betaUrl));
            server.serve("beta/skunkcrafts_updater_whitelist.txt", whitelist(crcOf("beta script content")));

            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(false));
            assertThat(sges.isSkunkcraftsBetaUpdateAvailable(), is(true));
            assertThat(sges.getSkunkcraftsBetaLatestVersion(), is("81.3-beta"));
            assertThat(sges.isAnySkunkcraftsUpdateAvailable(), is(true));
        }
    }

    @Test
    void sgesBetaSuppressedWhenBetaWhitelistIdentical() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            String stableUrl = server.baseUrl() + "sges";
            String betaUrl = server.baseUrl() + "beta";
            // local is behind on both channels, but beta carries the same content as stable
            Path luaFile = installedSges("Scripts", sgesCfg(stableUrl), betaCfg(betaUrl));
            server.serve("sges/skunkcrafts_updater.cfg", remoteCfg("81.2", stableUrl));
            String sharedWhitelist = whitelist(crcOf("newer script content"));
            server.serve("sges/skunkcrafts_updater_whitelist.txt", sharedWhitelist);
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("Beta", betaUrl));
            server.serve("beta/skunkcrafts_updater_whitelist.txt", sharedWhitelist);

            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(true));
            assertThat(sges.isSkunkcraftsBetaUpdateAvailable(), is(false));
        }
    }

    @Test
    void sgesDoesNotClaimAForeignBetaUpdaterCfg() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            // stable module URL serves nothing on this fixture: the check fails fast, no real network
            Path luaFile = installedSges("Scripts", sgesCfg(server.baseUrl() + "sges"),
                    "locked|false\nzone|custom\nname|BetterPushback\nmodule|https://example.com/betterpushback\nversion|1.6.2\n");

            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.getSkunkcraftsBetaConfig(), is(nullValue()));
            assertThat(sges.isSkunkcraftsBetaUpdateAvailable(), is(false));
        }
    }

    @Test
    void applyBetaUpdateDownloadsFromBetaModuleAndWritesBetaVersion() throws Exception {
        try (SkunkcraftsServerFixture server = SkunkcraftsServerFixture.start()) {
            String stableUrl = server.baseUrl() + "sges";
            String betaUrl = server.baseUrl() + "beta";
            Path luaFile = installedSges("Scripts", sgesCfg(stableUrl), betaCfg(betaUrl));
            server.serve("sges/skunkcrafts_updater.cfg", remoteCfg("81.2", stableUrl));
            server.serve("sges/skunkcrafts_updater_whitelist.txt", whitelist(SkunkcraftsUpdater.computeCRC32(luaFile)));
            server.serve("beta/skunkcrafts_updater.cfg", remoteCfg("Beta", betaUrl));
            server.serve("beta/skunkcrafts_updater_whitelist.txt", whitelist(crcOf("beta script content")));
            server.serve("beta/Scripts/" + SCRIPT, "beta script content");

            Sges sges = new Sges(new XPlane(tempDir), luaFile);
            sges.applySkunkcraftsBetaUpdate((p, m) -> {});

            assertThat(Files.readString(luaFile), is("beta script content"));
            String cfgContent = Files.readString(flyWithLuaFolder().resolve(CFG));
            assertThat(cfgContent, containsString("version|Beta"));
            // the stable module URL is never rewritten: that keeps the official updater compatible
            assertThat(cfgContent, containsString("module|" + stableUrl));
        }
    }

    @Test
    void sgesDoesNotClaimAForeignUpdaterCfg() throws Exception {
        // the FlyWithLua folder is the addon base folder of every FlyWithLua addon:
        // a config belonging to another one is not SGES's to claim
        Sges sges = new Sges(new XPlane(tempDir), installedSges("Scripts",
                "disabled|false\nzone|custom\nname|BetterPushback\nmodule|https://example.com/betterpushback\nversion|1.6.2\nlocked|false\n"));

        assertThat(sges.isSkunkcraftsUpdatable(), is(false));
        assertThat(sges.isSkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void disabledSgesIsNotUpdatable() throws Exception {
        // applying an update while SGES sits in Scripts (disabled)/ would download the
        // whitelist into the active Scripts folder, duplicating the script
        Sges sges = new Sges(new XPlane(tempDir),
                installedSges("Scripts (disabled)", sgesCfg("https://example.com/sges")));

        assertThat(sges.isSkunkcraftsUpdatable(), is(false));
        assertThat(sges.isSkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void flyWithLuaPluginDoesNotClaimTheSgesUpdaterCfg() throws Exception {
        // with the SGES cfg sitting in its base folder, the plugin must not offer the update
        installedSges("Scripts", sgesCfg("https://example.com/sges"));
        Path xplFile = tempDir.resolve("Resources/plugins/FlyWithLua/64/FlyWithLua.xpl");
        Files.createDirectories(xplFile.getParent());
        Files.writeString(xplFile, "");

        FlyWithLua flyWithLua = new FlyWithLua(new XPlane(tempDir), xplFile);

        assertThat(flyWithLua.isSkunkcraftsUpdatable(), is(false));
    }

    // Fixture helpers

    /** Installs SGES (script in the given Scripts folder + updater cfg, optionally beta cfg, in the FlyWithLua folder). */
    private Path installedSges(String scriptsFolder, String cfgContent, String betaCfgContent) throws IOException {
        Path flyWithLuaFolder = flyWithLuaFolder();
        Path luaFile = flyWithLuaFolder.resolve(scriptsFolder + "/" + SCRIPT);
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- SGES test stub\n");
        Files.writeString(flyWithLuaFolder.resolve(CFG), cfgContent);
        if (betaCfgContent != null) {
            Files.writeString(flyWithLuaFolder.resolve("skunkcrafts_updater_beta.cfg"), betaCfgContent);
        }
        return luaFile;
    }

    private Path installedSges(String scriptsFolder, String cfgContent) throws IOException {
        return installedSges(scriptsFolder, cfgContent, null);
    }

    private Path flyWithLuaFolder() {
        return tempDir.resolve("Resources/plugins/FlyWithLua");
    }

    private static String sgesCfg(String moduleUrl) {
        return "disabled|false\nzone|custom\nname|Simple Ground Equip. & Services\nmodule|" + moduleUrl
                + "\nversion|80.1\nlocked|false\n";
    }

    /** The local beta cfg ships without a version line; the beta label comes from the beta module. */
    private static String betaCfg(String moduleUrl) {
        return "disabled|false\nzone|custom\nname|Simple Ground Equip. & Services\nmodule|" + moduleUrl + "\nlocked|false\n";
    }

    /** A remote cfg body with the given version whose module points back at its own location. */
    private static String remoteCfg(String version, String moduleUrl) {
        return "name|Simple Ground Equip. & Services\nversion|" + version + "\nmodule|" + moduleUrl
                + "\ndisabled|false\nlocked|false\n";
    }

    private static String whitelist(long crc) {
        return "Scripts/" + SCRIPT + "|" + crc + "|99\n";
    }

    private static long crcOf(String content) {
        CRC32 crc = new CRC32();
        crc.update(content.getBytes(StandardCharsets.UTF_8));
        return crc.getValue();
    }
}
