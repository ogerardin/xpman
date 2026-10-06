package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.scenery.SceneryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SceneryManagerSystemExclusionTest {

    @TempDir
    Path tempDir;

    @Test
    void systemFoldersAreExcludedFromEntries() throws Exception {
        // system folder: under Global Scenery
        Path globalScenery = tempDir.resolve("Custom Scenery").resolveSibling("Global Scenery");
        Files.createDirectories(globalScenery.resolve("System Pack"));

        // user folder: under Custom Scenery
        Path customScenery = tempDir.resolve("Custom Scenery");
        Files.createDirectories(customScenery.resolve("User Pack"));

        XPlane xPlane = new XPlane(tempDir);
        SceneryManager manager = xPlane.getSceneryManager();
        manager.loadPackages();
        List<?> entries = manager.getSceneryEntries();

        long userCount = entries.stream()
                .map(Object::toString)
                .filter(s -> s.contains("User Pack"))
                .count();
        long systemCount = entries.stream()
                .map(Object::toString)
                .filter(s -> s.contains("System Pack"))
                .count();

        assertThat("user pack should appear in entries", userCount, is(1L));
        assertThat("system pack should be excluded from entries", systemCount, is(0L));
    }
}
