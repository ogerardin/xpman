package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests that installation skips root-level files (manuals, readmes, etc.)
 * and only extracts entries inside the root folder.
 */
class InstallationSkipsRootFilesTest {

    @TempDir
    Path tempDir;

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    @Test
    void pluginInstallationSkipsRootLevelManual() throws Exception {
        // Create a plugin archive with root-level Manual.pdf
        Path zipFile = tempDir.resolve("plugin.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("MyPlugin/"));
            out.putNextEntry(new ZipEntry("MyPlugin/mac_x64/"));
            out.putNextEntry(new ZipEntry("MyPlugin/mac_x64/MyPlugin.xpl"));
            out.write("fake plugin".getBytes());
            out.putNextEntry(new ZipEntry("Manual.pdf"));
            out.write("fake manual".getBytes());
        }

        // Create a fake X-Plane installation
        Path xpRoot = tempDir.resolve("X-Plane");
        Files.createDirectories(xpRoot.resolve("Resources/plugins"));

        XPlane xplane = new XPlane(xpRoot);
        PluginManager pluginManager = xplane.getPluginManager();

        // Install the plugin
        pluginManager.install(new ZipArchive(zipFile), NO_PROGRESS);

        // Verify plugin was installed
        Path pluginFolder = xpRoot.resolve("Resources/plugins/MyPlugin");
        assertThat("Plugin folder should exist", Files.exists(pluginFolder));
        assertThat("Plugin .xpl should exist",
                Files.exists(pluginFolder.resolve("mac_x64/MyPlugin.xpl")));

        // Verify root-level Manual.pdf was NOT installed
        assertThat("Root-level Manual.pdf should NOT be extracted",
                !Files.exists(xpRoot.resolve("Manual.pdf")));
        assertThat("Manual.pdf should NOT be in plugin folder",
                !Files.exists(pluginFolder.resolve("Manual.pdf")));
    }

    @Test
    void sceneryInstallationSkipsRootLevelReadme() throws Exception {
        // Create a scenery archive with root-level README.txt
        Path zipFile = tempDir.resolve("scenery.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("MyLibrary/"));
            out.putNextEntry(new ZipEntry("MyLibrary/some_file.obj"));
            out.write("fake object".getBytes());
            out.putNextEntry(new ZipEntry("README.txt"));
            out.write("fake readme".getBytes());
        }

        // Create a fake X-Plane installation
        Path xpRoot = tempDir.resolve("X-Plane");
        Files.createDirectories(xpRoot.resolve("Custom Scenery"));

        XPlane xplane = new XPlane(xpRoot);
        xplane.getSceneryManager().install(new ZipArchive(zipFile), NO_PROGRESS);

        // Verify scenery was installed
        Path sceneryFolder = xpRoot.resolve("Custom Scenery/MyLibrary");
        assertThat("Scenery folder should exist", Files.exists(sceneryFolder));
        assertThat("Scenery file should exist",
                Files.exists(sceneryFolder.resolve("some_file.obj")));

        // Verify root-level README.txt was NOT installed
        assertThat("Root-level README.txt should NOT be extracted",
                !Files.exists(xpRoot.resolve("README.txt")));
    }

    @Test
    void aircraftInstallationSkipsRootLevelFile() throws Exception {
        // Create an aircraft archive with root-level Manual.pdf
        Path zipFile = tempDir.resolve("aircraft.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("MyAircraft/"));
            out.putNextEntry(new ZipEntry("MyAircraft/MyAircraft.acf"));
            out.write("fake aircraft".getBytes());
            out.putNextEntry(new ZipEntry("Manual.pdf"));
            out.write("fake manual".getBytes());
        }

        // Create a fake X-Plane installation
        Path xpRoot = tempDir.resolve("X-Plane");
        Files.createDirectories(xpRoot.resolve("Aircraft"));

        XPlane xplane = new XPlane(xpRoot);
        xplane.getAircraftManager().install(new ZipArchive(zipFile), NO_PROGRESS);

        // Verify aircraft was installed
        Path aircraftFolder = xpRoot.resolve("Aircraft/MyAircraft");
        assertThat("Aircraft folder should exist", Files.exists(aircraftFolder));
        assertThat("Aircraft .acf should exist",
                Files.exists(aircraftFolder.resolve("MyAircraft.acf")));

        // Verify root-level Manual.pdf was NOT installed
        assertThat("Root-level Manual.pdf should NOT be extracted",
                !Files.exists(xpRoot.resolve("Manual.pdf")));
        assertThat("Manual.pdf should NOT be in aircraft folder",
                !Files.exists(aircraftFolder.resolve("Manual.pdf")));
    }
}
