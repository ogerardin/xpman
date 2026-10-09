package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.scenery.SceneryEntry;
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

    @Test
    void updateEligibilityFollowsIniDisabledState() throws Exception {
        Path infoPlist = tempDir.resolve("X-Plane.app/Contents/info.plist");
        Files.createDirectories(infoPlist.getParent());
        Files.writeString(infoPlist, """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                <plist version="1.0"><dict>
                <key>CFBundleShortVersionString</key><string>12.0</string>
                <key>CFBundleExecutable</key><string>X-Plane</string>
                </dict></plist>
                """);
        Path customScenery = tempDir.resolve("Custom Scenery");
        Path packageFolder = customScenery.resolve("User Pack");
        Files.createDirectories(packageFolder);
        Files.writeString(packageFolder.resolve("skunkcrafts_updater.cfg"), "module|https://example.com/\n");
        Files.writeString(customScenery.resolve("scenery_packs.ini"), """
                I
                1000 Version
                SCENERY

                SCENERY_PACK_DISABLED Custom Scenery/User Pack
                """);

        SceneryManager manager = new XPlane(tempDir).getSceneryManager();
        manager.loadPackages();
        SceneryEntry entry = manager.getSceneryEntries().getFirst();

        assertThat(entry.getSceneryPackage().isEnabled(), is(false));
        assertThat(entry.getSceneryPackage().isSkunkcraftsUpdatable(), is(false));
        manager.enable(entry);
        assertThat(entry.getSceneryPackage().isEnabled(), is(true));
        assertThat(entry.getSceneryPackage().isSkunkcraftsUpdatable(), is(true));
    }
}
