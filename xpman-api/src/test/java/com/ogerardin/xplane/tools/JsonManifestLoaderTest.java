package com.ogerardin.xplane.tools;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class JsonManifestLoaderTest {

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
}
