package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.plugins.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for the plugin trim-to-variant functionality.
 */
class PluginTrimTest {

    @TempDir
    Path tempDir;

    /**
     * Creates a plugin folder structure with LST-style multiple binaries:
     * - Legacy root-level .xpl files (mac.xpl, win.xpl, lin.xpl)
     * - Modern platform-specific folders (mac_x64, win_x64, lin_x64)
     * - A platform-neutral folder (64)
     */
    private Path createLstStylePlugin(String pluginName) throws IOException {
        Path pluginFolder = tempDir.resolve("Resources/plugins/" + pluginName);
        Files.createDirectories(pluginFolder);

        // Legacy root-level binaries
        Files.createFile(pluginFolder.resolve("mac.xpl"));
        Files.createFile(pluginFolder.resolve("win.xpl"));
        Files.createFile(pluginFolder.resolve("lin.xpl"));

        // Modern platform-specific folders
        Path macX64 = pluginFolder.resolve("mac_x64");
        Path winX64 = pluginFolder.resolve("win_x64");
        Path linX64 = pluginFolder.resolve("lin_x64");
        Files.createDirectories(macX64);
        Files.createDirectories(winX64);
        Files.createDirectories(linX64);

        Files.createFile(macX64.resolve(pluginName + ".xpl"));
        Files.createFile(winX64.resolve(pluginName + ".xpl"));
        Files.createFile(linX64.resolve(pluginName + ".xpl"));

        // Platform-neutral folder
        Path neutral = pluginFolder.resolve("64");
        Files.createDirectories(neutral);
        Files.createFile(neutral.resolve("neutral.xpl"));

        // Some non-.xpl files that should not be removed
        Files.createFile(pluginFolder.resolve("skunkcrafts_updater.cfg"));
        Files.createFile(pluginFolder.resolve("README.md"));

        return pluginFolder;
    }

    @Test
    void trimToVariantForMacRemovesNonMacBinaries() throws Exception {
        // Create plugin with multiple binaries
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("mac_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        // Verify initial state
        List<Path> allXpls = plugin.getAllXplFiles();
        assertThat("Should have 7 .xpl files initially", allXpls.size(), is(7));

        // Trim to MAC variant
        List<Path> removed = plugin.trimToVariant(XPlaneVariant.MAC);

        // Verify removed items
        assertThat("Should remove 5 items (3 legacy + 2 platform folders)", removed.size(), is(5));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should have 2 .xpl files remaining (mac_x64 + 64/)", remainingXpls.size(), is(2));

        // Check specific files/folders
        assertThat("mac_x64 folder should exist", Files.exists(pluginFolder.resolve("mac_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        assertThat("win_x64 folder should not exist", !Files.exists(pluginFolder.resolve("win_x64")));
        assertThat("lin_x64 folder should not exist", !Files.exists(pluginFolder.resolve("lin_x64")));
        assertThat("Legacy mac.xpl should not exist", !Files.exists(pluginFolder.resolve("mac.xpl")));
        assertThat("Legacy win.xpl should not exist", !Files.exists(pluginFolder.resolve("win.xpl")));
        assertThat("Legacy lin.xpl should not exist", !Files.exists(pluginFolder.resolve("lin.xpl")));

        // Non-.xpl files should remain
        assertThat("skunkcrafts_updater.cfg should exist", Files.exists(pluginFolder.resolve("skunkcrafts_updater.cfg")));
        assertThat("README.md should exist", Files.exists(pluginFolder.resolve("README.md")));
    }

    @Test
    void trimToVariantForWindowsRemovesNonWindowsBinaries() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("win_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        // Trim to WINDOWS variant
        List<Path> removed = plugin.trimToVariant(XPlaneVariant.WINDOWS);

        assertThat("Should remove 5 items", removed.size(), is(5));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should have 2 .xpl files remaining (win_x64 + 64/)", remainingXpls.size(), is(2));

        assertThat("win_x64 folder should exist", Files.exists(pluginFolder.resolve("win_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        assertThat("mac_x64 folder should not exist", !Files.exists(pluginFolder.resolve("mac_x64")));
        assertThat("lin_x64 folder should not exist", !Files.exists(pluginFolder.resolve("lin_x64")));
    }

    @Test
    void trimToVariantForLinuxRemovesNonLinuxBinaries() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("lin_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        // Trim to LINUX variant
        List<Path> removed = plugin.trimToVariant(XPlaneVariant.LINUX);

        assertThat("Should remove 5 items", removed.size(), is(5));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should have 2 .xpl files remaining (lin_x64 + 64/)", remainingXpls.size(), is(2));

        assertThat("lin_x64 folder should exist", Files.exists(pluginFolder.resolve("lin_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        assertThat("mac_x64 folder should not exist", !Files.exists(pluginFolder.resolve("mac_x64")));
        assertThat("win_x64 folder should not exist", !Files.exists(pluginFolder.resolve("win_x64")));
    }

    @Test
    void getNonApplicableXplFilesReturnsCorrectFiles() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("mac_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        List<Path> nonApplicable = plugin.getNonApplicableXplFiles(XPlaneVariant.MAC);

        assertThat("Should have 5 non-applicable .xpl files", nonApplicable.size(), is(5));

        // Check that the list includes legacy files and platform folders
        List<String> nonApplicableNames = nonApplicable.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        assertThat("Should include mac.xpl", nonApplicableNames, hasItem("mac.xpl"));
        assertThat("Should include win.xpl", nonApplicableNames, hasItem("win.xpl"));
        assertThat("Should include lin.xpl", nonApplicableNames, hasItem("lin.xpl"));
    }

    @Test
    void getApplicableXplFilesReturnsCorrectFiles() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("mac_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        List<Path> applicable = plugin.getApplicableXplFiles(XPlaneVariant.MAC);

        assertThat("Should have 2 applicable .xpl files", applicable.size(), is(2));

        List<String> applicableNames = applicable.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        assertThat("Should include TestPlugin.xpl from mac_x64", applicableNames, hasItem("TestPlugin.xpl"));
        assertThat("Should include neutral.xpl from 64/", applicableNames, hasItem("neutral.xpl"));
    }

    @Test
    void trimWithNoNonApplicableBinariesReturnsEmptyList() throws Exception {
        // Create a plugin with only applicable binaries
        Path pluginFolder = tempDir.resolve("Resources/plugins/SimplePlugin");
        Files.createDirectories(pluginFolder);
        Path xplFile = pluginFolder.resolve("SimplePlugin.xpl");
        Files.createFile(xplFile);

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        List<Path> removed = plugin.trimToVariant(XPlaneVariant.MAC);

        assertThat("Should remove nothing", removed.size(), is(0));
        assertThat("Plugin .xpl should still exist", Files.exists(xplFile));
    }
}
