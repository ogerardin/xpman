package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
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
 * Tests version reporting of plugins distributed via X-Plane.org.
 * A Skunkcrafts-managed addon must report the Skunkcrafts module version, not the
 * x-plane.org page version: the two are unrelated version namespaces, and the update
 * pill shown for such an addon is driven by the Skunkcrafts check.
 */
class XPlaneOrgPluginTest {

    @TempDir
    Path tempDir;

    @Test
    void latestVersionComesFromSkunkcraftsModuleWhenUpdatable() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/sges/skunkcrafts_updater.cfg",
                exchange -> respond(exchange, "name|SGES\nversion|81.2\nmodule|http://127.0.0.1:" + port + "/sges\n"));
        server.createContext("/orgpage",
                exchange -> respond(exchange, "<html>\"softwareVersion\": \"2.8.17\"</html>"));
        server.start();
        try {
            Path pluginFolder = tempDir.resolve("Resources/plugins/FlyWithLua");
            Files.createDirectories(pluginFolder);
            Path xplFile = pluginFolder.resolve("FlyWithLua.xpl");
            Files.write(xplFile, new byte[0]);
            Files.writeString(pluginFolder.resolve("skunkcrafts_updater.cfg"),
                    "name|SGES\nversion|80.1\nmodule|http://127.0.0.1:" + port + "/sges\n");

            XPlaneOrgPlugin plugin = new XPlaneOrgPlugin(new XPlane(tempDir), xplFile,
                    "FlyWithLua", "test plugin", "http://127.0.0.1:" + port + "/orgpage");

            assertThat(plugin.isSkunkcraftsUpdatable(), is(true));
            assertThat(plugin.getLatestVersion(), is("81.2"));
        } finally {
            server.stop(0);
        }
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
