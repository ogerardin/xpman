package com.ogerardin.xplane.test.skunkcrafts;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serves the remote side of the Skunkcrafts protocol (cfg, beta cfg, whitelists, blacklists, files)
 * from memory for tests.
 */
public class SkunkcraftsServerFixture implements AutoCloseable {

    private final HttpServer server;
    private final Map<String, String> content = new ConcurrentHashMap<>();

    private SkunkcraftsServerFixture(HttpServer server) {
        this.server = server;
    }

    public static SkunkcraftsServerFixture start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        SkunkcraftsServerFixture fixture = new SkunkcraftsServerFixture(server);
        server.createContext("/", fixture::handle);
        server.start();
        return fixture;
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    /** Serves the body at the given path (no leading slash, e.g. "stable/skunkcrafts_updater.cfg"); 404 otherwise. */
    public SkunkcraftsServerFixture serve(String path, String body) {
        content.put(path, body);
        return this;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().replaceFirst("^/", "");
        String body = content.get(path);
        if (body == null) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
