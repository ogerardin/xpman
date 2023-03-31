package com.ogerardin.xplane.test.tools;

import com.ogerardin.xplane.tools.JsonManifestLoader;
import com.ogerardin.xplane.tools.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
class ManifestTest {

    @Test
    @SuppressWarnings("ConstantConditions")
    public void testGsonDeserialize() throws URISyntaxException {
        Path manifestFile = Paths.get(getClass().getResource("/tools/opensceneryx-installer.json").toURI());

        Manifest manifest = JsonManifestLoader.loadManifest(manifestFile);

        log.info("parsed: {}", manifest);
    }
}