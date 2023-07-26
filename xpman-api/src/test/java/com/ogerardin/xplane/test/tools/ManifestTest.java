package com.ogerardin.xplane.test.tools;

import com.ogerardin.xplane.tools.JsonManifestLoader;
import com.ogerardin.xplane.tools.Manifest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
class ManifestTest {

    @Test
    public void testGsonDeserialize() throws URISyntaxException {
        Path manifestFile = Paths.get(Objects.requireNonNull(getClass().getResource("/test/tools/x-updater.json")).toURI());

        Manifest manifest = JsonManifestLoader.loadManifest(manifestFile);

        log.info("parsed: {}", manifest);
    }

    @Test
    public void testUnfoldManifest() throws URISyntaxException {
        Path manifestFile = Paths.get(Objects.requireNonNull(getClass().getResource("/test/tools/x-updater.json")).toURI());
        Manifest manifest = JsonManifestLoader.loadManifest(manifestFile);

        List<Manifest> unfoldedManifests = manifest.unfold();
        unfoldedManifests.forEach(m -> log.info("unfolded: {}", m));

        assertThat(unfoldedManifests.size(), is(3));
    }
}