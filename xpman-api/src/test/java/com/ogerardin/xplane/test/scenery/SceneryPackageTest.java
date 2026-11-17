package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SceneryPackageTest {

    @Test
    void versionShouldPreferSkunkcraftsConfigOverVersionFile(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("skunkcrafts_updater.cfg"), "version|1.14\n");
        Files.writeString(folder.resolve("version.txt"), "2.0c\n");

        assertEquals("1.14", new SceneryPackage(folder).getVersion());
    }

    @Test
    void versionShouldFallBackToVersionFile(@TempDir Path folder) throws IOException {
        Files.writeString(folder.resolve("version.txt"), "2.0c\n");

        assertEquals("2.0c", new SceneryPackage(folder).getVersion());
    }

    @Test
    void versionShouldReturnNullWhenNeitherConfigNorVersionFileExists(@TempDir Path folder) {
        assertNull(new SceneryPackage(folder).getVersion());
    }

    @Test
    void misterXLibraryResolvesToBaseSceneryPackage(@TempDir Path folder) throws Exception {
        // Simulate an installed MisterX Library folder
        Path misterX = folder.resolve("MisterX_Library");
        Files.createDirectories(misterX);
        Files.writeString(misterX.resolve("library.txt"), "EXPORT dummy");
        Files.writeString(misterX.resolve("version.txt"), "2.0c\n");

        SceneryPackage pkg = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, misterX);

        // Should resolve to the base class (no specialized MisterX subclass exists)
        assertEquals(SceneryPackage.class, pkg.getClass());
        assertEquals("2.0c", pkg.getVersion());
    }
}
