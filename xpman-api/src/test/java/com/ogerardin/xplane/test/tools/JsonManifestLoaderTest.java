package com.ogerardin.xplane.test.tools;

import com.ogerardin.xplane.tools.JsonManifestLoader;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.tools.ToolIcon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class JsonManifestLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadManifestWithIcon() throws Exception {
        String json = """
            {
              "name": "Test Tool",
              "icon": "https://example.com/icon.png",
              "description": "Test description",
              "version": "1.0.0",
              "url": "https://example.com/tool.zip",
              "file": "tool.exe"
            }
            """;
        try (InputStream is = new ByteArrayInputStream(json.getBytes())) {
            Manifest manifest = JsonManifestLoader.loadManifest(is, "test.json");
            assertNotNull(manifest.icon());
            assertInstanceOf(ToolIcon.Url.class, manifest.icon());
            assertEquals("https://example.com/icon.png", ((ToolIcon.Url) manifest.icon()).url().toString());
        }
    }

    @Test
    void loadManifestWithVersionRegex() throws Exception {
        String json = """
            {
              "name": "Test Tool",
              "versionRegex": "(?<![\\\\d.])(\\\\d+\\\\.\\\\d+[a-z]?)\\\\.go(?!\\\\x00)"
            }
            """;
        try (InputStream is = new ByteArrayInputStream(json.getBytes())) {
            Manifest manifest = JsonManifestLoader.loadManifest(is, "test.json");
            assertNotNull(manifest.versionRegex());
            assertEquals("(?<![\\d.])(\\d+\\.\\d+[a-z]?)\\.go(?!\\x00)", manifest.versionRegex().pattern());
            // the loaded pattern must work end-to-end against the kind of content it was written for
            var matcher = manifest.versionRegex().matcher("Plugins3.2e.goversionenabled");
            assertTrue(matcher.find());
            assertEquals("3.2e", matcher.group(1));
        }
    }

    @Test
    void installCheckerFindsStringInBinaryFile() throws Exception {
        String json = """
                {"installChecker":{"string":"1.3.3"}}
                """;
        Manifest manifest = JsonManifestLoader.loadManifest(new ByteArrayInputStream(json.getBytes()), "test.json");
        Path binary = tempDir.resolve("binary.exe");
        Files.writeString(binary, "version 1.3.3", StandardCharsets.ISO_8859_1);

        assertTrue(manifest.installChecker().test(binary));
    }
}
