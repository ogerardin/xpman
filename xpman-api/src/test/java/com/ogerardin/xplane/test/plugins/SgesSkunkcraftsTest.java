package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.plugins.custom.lua.Sges;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests that SGES — the FlyWithLua script — is the Skunkcrafts-updatable addon, not the
 * FlyWithLua plugin hosting it: the updater cfg installed in the FlyWithLua folder belongs
 * to SGES, so the update must be offered on the script row, and the plugin must not claim it.
 */
class SgesSkunkcraftsTest {

    private static final String SCRIPT = "Simple_Ground_Equipment_and_Services.lua";

    @TempDir
    Path tempDir;

    @Test
    void sgesClaimsItsUpdaterCfgAndReportsTheRemoteVersion() throws Exception {
        HttpServer server = httpServer("81.2");
        server.start();
        try {
            Path luaFile = installedSges("Scripts", sgesCfg("http://127.0.0.1:" + port(server) + "/sges"));
            Sges sges = new Sges(new XPlane(tempDir), luaFile);

            assertThat(sges.isSkunkcraftsUpdatable(), is(true));
            // the FlyWithLua folder is the addon base folder the whitelist paths resolve against
            assertThat(sges.getSkunkcraftsFolder(), is(luaFile.getParent().getParent()));
            // the installed version is the cfg's, not the lua header's
            assertThat(sges.getInstalledVersion(), is("80.1"));
            assertThat(sges.getSkunkcraftsLatestVersion(), is("81.2"));
            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(true));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void upToDateSgesReportsNoUpdate() throws Exception {
        HttpServer server = httpServer("80.1");
        server.start();
        try {
            Sges sges = new Sges(new XPlane(tempDir),
                    installedSges("Scripts", sgesCfg("http://127.0.0.1:" + port(server) + "/sges")));

            assertThat(sges.isSkunkcraftsUpdateAvailable(), is(false));
        } finally {
            server.stop(0);
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

    /** Installs SGES (script in the given Scripts folder + updater cfg in the FlyWithLua folder). */
    private Path installedSges(String scriptsFolder, String cfgContent) throws IOException {
        Path flyWithLuaFolder = tempDir.resolve("Resources/plugins/FlyWithLua");
        Path luaFile = flyWithLuaFolder.resolve(scriptsFolder + "/" + SCRIPT);
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- SGES test stub\n");
        Files.writeString(flyWithLuaFolder.resolve("skunkcrafts_updater.cfg"), cfgContent);
        return luaFile;
    }

    private static String sgesCfg(String moduleUrl) {
        return "disabled|false\nzone|custom\nname|Simple Ground Equip. & Services\nmodule|" + moduleUrl
                + "\nversion|80.1\nlocked|false\n";
    }

    /** Serves the remote version cfg the Skunkcrafts check fetches from the module URL. */
    private static HttpServer httpServer(String remoteVersion) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/sges/skunkcrafts_updater.cfg",
                exchange -> respond(exchange, "name|Simple Ground Equip. & Services\nversion|" + remoteVersion + "\n"));
        return server;
    }

    private static int port(HttpServer server) {
        return server.getAddress().getPort();
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
