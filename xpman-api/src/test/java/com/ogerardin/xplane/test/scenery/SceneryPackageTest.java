package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.scenery.SceneryPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SceneryPackageTest {

    @Test
    void versionShouldFallBackToSkunkcraftsConfig(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.14\n");

        assertEquals("1.14", new SceneryPackage(folder).getVersion());
    }
}
