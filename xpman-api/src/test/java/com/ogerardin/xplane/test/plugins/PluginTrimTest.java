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

        // Legacy root-level binaries with correct magic bytes
        createMachOBinary(pluginFolder.resolve("mac.xpl"));
        createPeBinary(pluginFolder.resolve("win.xpl"));
        createElfBinary(pluginFolder.resolve("lin.xpl"));

        // Modern platform-specific folders with correct magic bytes
        Path macX64 = pluginFolder.resolve("mac_x64");
        Path winX64 = pluginFolder.resolve("win_x64");
        Path linX64 = pluginFolder.resolve("lin_x64");
        Files.createDirectories(macX64);
        Files.createDirectories(winX64);
        Files.createDirectories(linX64);

        createMachOBinary(macX64.resolve(pluginName + ".xpl"));
        createPeBinary(winX64.resolve(pluginName + ".xpl"));
        createElfBinary(linX64.resolve(pluginName + ".xpl"));

        // X-Plane's 64/ convention selects platform binaries by file contents, not folder name.
        Path neutral = pluginFolder.resolve("64");
        Files.createDirectories(neutral);
        createMachOBinary(neutral.resolve("neutral_mac.xpl"));
        createPeBinary(neutral.resolve("neutral_win.xpl"));
        createElfBinary(neutral.resolve("neutral_lin.xpl"));

        // Some non-.xpl files that should not be removed
        Files.createFile(pluginFolder.resolve("skunkcrafts_updater.cfg"));
        Files.createFile(pluginFolder.resolve("README.md"));

        return pluginFolder;
    }

    /**
     * Creates a Mach-O binary with the correct magic bytes for macOS.
     */
    private void createMachOBinary(Path path) throws IOException {
        byte[] machOMagic = new byte[]{(byte) 0xCF, (byte) 0xFA, (byte) 0xED, (byte) 0xFE};
        Files.write(path, machOMagic);
    }

    /**
     * Creates a PE binary with the correct magic bytes for Windows.
     */
    private void createPeBinary(Path path) throws IOException {
        byte[] peMagic = new byte[0x44];
        peMagic[0] = 'M';
        peMagic[1] = 'Z';
        peMagic[0x3C] = 0x40; // e_lfanew = 0x40, little-endian
        peMagic[0x40] = 'P';
        peMagic[0x41] = 'E';
        Files.write(path, peMagic);
    }

    /**
     * Creates an ELF binary with the correct magic bytes for Linux.
     */
    private void createElfBinary(Path path) throws IOException {
        byte[] elfMagic = new byte[]{(byte) 0x7F, (byte) 0x45, (byte) 0x4C, (byte) 0x46};
        Files.write(path, elfMagic);
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
        assertThat("Should have 9 .xpl files initially", allXpls.size(), is(9));

        // Trim to MAC variant
        List<Path> removed = plugin.trimToVariant(XPlaneVariant.MAC);

        // Verify removed items
        assertThat("Should remove 6 binaries for other platforms", removed.size(), is(6));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should retain the three Mach-O binaries", remainingXpls.size(), is(3));

        // Check specific files/folders
        assertThat("mac_x64 folder should exist", Files.exists(pluginFolder.resolve("mac_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        // Empty platform folders should be deleted
        assertThat("Empty win_x64 folder should be deleted", Files.notExists(pluginFolder.resolve("win_x64")));
        assertThat("Empty lin_x64 folder should be deleted", Files.notExists(pluginFolder.resolve("lin_x64")));
        assertThat("Legacy mac.xpl should remain", Files.exists(pluginFolder.resolve("mac.xpl")));
        assertThat("Legacy win.xpl should be removed", Files.notExists(pluginFolder.resolve("win.xpl")));
        assertThat("Legacy lin.xpl should be removed", Files.notExists(pluginFolder.resolve("lin.xpl")));

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

        assertThat("Should remove 6 binaries for other platforms", removed.size(), is(6));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should retain the three PE binaries", remainingXpls.size(), is(3));

        assertThat("win_x64 folder should exist", Files.exists(pluginFolder.resolve("win_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        assertThat("mac_x64 binary should be removed", Files.notExists(pluginFolder.resolve("mac_x64/TestPlugin.xpl")));
        assertThat("lin_x64 binary should be removed", Files.notExists(pluginFolder.resolve("lin_x64/TestPlugin.xpl")));
    }

    @Test
    void trimToVariantForLinuxRemovesNonLinuxBinaries() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("lin_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        // Trim to LINUX variant
        List<Path> removed = plugin.trimToVariant(XPlaneVariant.LINUX);

        assertThat("Should remove 6 binaries for other platforms", removed.size(), is(6));

        // Verify what remains
        List<Path> remainingXpls = plugin.getAllXplFiles();
        assertThat("Should retain the three ELF binaries", remainingXpls.size(), is(3));

        assertThat("lin_x64 folder should exist", Files.exists(pluginFolder.resolve("lin_x64")));
        assertThat("64 folder should exist", Files.exists(pluginFolder.resolve("64")));
        assertThat("mac_x64 binary should be removed", Files.notExists(pluginFolder.resolve("mac_x64/TestPlugin.xpl")));
        assertThat("win_x64 binary should be removed", Files.notExists(pluginFolder.resolve("win_x64/TestPlugin.xpl")));
    }

    @Test
    void getNonApplicableXplFilesReturnsCorrectFiles() throws Exception {
        Path pluginFolder = createLstStylePlugin("TestPlugin");
        Path xplFile = pluginFolder.resolve("mac_x64/TestPlugin.xpl");

        XPlane xplane = new XPlane(tempDir);
        Plugin plugin = new Plugin(xplane, xplFile);

        List<Path> nonApplicable = plugin.getNonApplicableXplFiles(XPlaneVariant.MAC);

        assertThat("Should have 6 non-applicable .xpl files", nonApplicable.size(), is(6));

        // Check that the list includes legacy files and platform folders
        List<String> nonApplicableNames = nonApplicable.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        assertThat("Should not include the applicable mac.xpl", nonApplicableNames, not(hasItem("mac.xpl")));
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

        assertThat("Should have 3 applicable .xpl files", applicable.size(), is(3));

        List<String> applicableNames = applicable.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        assertThat("Should include TestPlugin.xpl from mac_x64", applicableNames, hasItem("TestPlugin.xpl"));
        assertThat("Should include neutral_mac.xpl from 64/", applicableNames, hasItem("neutral_mac.xpl"));
        assertThat("Should not include neutral_win.xpl from 64/", applicableNames, not(hasItem("neutral_win.xpl")));
        assertThat("Should not include neutral_lin.xpl from 64/", applicableNames, not(hasItem("neutral_lin.xpl")));
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

    @Test
    void unknownBinaryIsNotOfferedForDeletion() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/MixedPlugin");
        Files.createDirectories(pluginFolder);
        Path macBinary = pluginFolder.resolve("mac.xpl");
        createMachOBinary(macBinary);
        Path unknownBinary = pluginFolder.resolve("unknown.xpl");
        Files.writeString(unknownBinary, "not a recognized executable format");

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);

        assertThat(plugin.getNonApplicableXplFiles(XPlaneVariant.MAC), not(hasItem(unknownBinary)));
        assertThat(plugin.trimToVariant(XPlaneVariant.MAC), not(hasItem(unknownBinary)));
        assertThat(Files.exists(unknownBinary), is(true));
    }

    @Test
    void unknownTargetVariantDoesNotOfferAnyFilesForDeletion() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/MixedPlugin");
        Files.createDirectories(pluginFolder);
        Path macBinary = pluginFolder.resolve("mac.xpl");
        createMachOBinary(macBinary);
        Path windowsBinary = pluginFolder.resolve("win.xpl");
        createPeBinary(windowsBinary);
        Path linuxBinary = pluginFolder.resolve("lin.xpl");
        createElfBinary(linuxBinary);

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);

        assertThat(plugin.getNonApplicableXplFiles(XPlaneVariant.UNKNOWN), is(empty()));
        assertThat(plugin.trimToVariant(XPlaneVariant.UNKNOWN), is(empty()));
        assertThat(Files.exists(macBinary) && Files.exists(windowsBinary) && Files.exists(linuxBinary), is(true));
    }

    @Test
    void pluginAdminKeepsMacBinaryAndOffersOnlyWindowsAndLinuxFrom64Folder() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/PluginAdmin");
        Files.createDirectories(pluginFolder.resolve("64"));
        Path macBinary = pluginFolder.resolve("mac.xpl");
        Path linuxBinary = pluginFolder.resolve("64/lin.xpl");
        Path windowsBinary = pluginFolder.resolve("64/win.xpl");
        createMachOBinary(macBinary);
        createElfBinary(linuxBinary);
        createPeBinary(windowsBinary);

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);

        assertThat(plugin.getNonApplicableXplFiles(XPlaneVariant.MAC), containsInAnyOrder(linuxBinary, windowsBinary));
        assertThat(plugin.trimToVariant(XPlaneVariant.MAC), containsInAnyOrder(linuxBinary, windowsBinary));
        assertThat("The only Mac binary must not be deleted", Files.exists(macBinary), is(true));
    }

    @Test
    void trimOnSkunkcraftsManagedPluginAddsRemovedFilesToIgnoreFile() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/LST");
        Files.createDirectories(pluginFolder.resolve("mac_x64"));
        Files.createDirectories(pluginFolder.resolve("win_x64"));
        Path macBinary = pluginFolder.resolve("mac_x64/LST.xpl");
        Path windowsBinary = pluginFolder.resolve("win_x64/LST.xpl");
        createMachOBinary(macBinary);
        createPeBinary(windowsBinary);
        // Skunkcrafts config makes the plugin Skunkcrafts-managed
        Files.writeString(pluginFolder.resolve("skunkcrafts_updater.cfg"), "module|https://example.com/\n");

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);
        assertThat(plugin.isSkunkcraftsManaged(), is(true));

        plugin.trimToVariant(XPlaneVariant.MAC);

        Path ignoreFile = pluginFolder.resolve("skunkcrafts_updater_ignore.txt");
        assertThat("Ignore file should be created", Files.exists(ignoreFile), is(true));
        String content = Files.readString(ignoreFile);
        assertThat("Should contain the trimmed binary's relative path", content, containsString("win_x64/LST.xpl"));
        assertThat("Should NOT contain the kept binary", content, not(containsString("mac_x64/LST.xpl")));
    }

    @Test
    void trimOnSkunkcraftsManagedPluginPreservesExistingIgnoreEntries() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/LST");
        Files.createDirectories(pluginFolder.resolve("mac_x64"));
        Files.createDirectories(pluginFolder.resolve("win_x64"));
        Path macBinary = pluginFolder.resolve("mac_x64/LST.xpl");
        Path windowsBinary = pluginFolder.resolve("win_x64/LST.xpl");
        createMachOBinary(macBinary);
        createPeBinary(windowsBinary);
        Files.writeString(pluginFolder.resolve("skunkcrafts_updater.cfg"), "module|https://example.com/\n");
        // Pre-existing ignore entries and comments must survive the trim
        Files.writeString(pluginFolder.resolve("skunkcrafts_updater_ignore.txt"),
                "# user comment\nexisting/file.txt\n");

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);
        plugin.trimToVariant(XPlaneVariant.MAC);

        String content = Files.readString(pluginFolder.resolve("skunkcrafts_updater_ignore.txt"));
        assertThat(content, containsString("# user comment"));
        assertThat(content, containsString("existing/file.txt"));
        assertThat(content, containsString("win_x64/LST.xpl"));
    }

    @Test
    void trimOnNonSkunkcraftsPluginDoesNotCreateIgnoreFile() throws Exception {
        Path pluginFolder = tempDir.resolve("Resources/plugins/LST");
        Files.createDirectories(pluginFolder.resolve("mac_x64"));
        Files.createDirectories(pluginFolder.resolve("win_x64"));
        Path macBinary = pluginFolder.resolve("mac_x64/LST.xpl");
        Path windowsBinary = pluginFolder.resolve("win_x64/LST.xpl");
        createMachOBinary(macBinary);
        createPeBinary(windowsBinary);

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);
        assertThat(plugin.isSkunkcraftsManaged(), is(false));

        plugin.trimToVariant(XPlaneVariant.MAC);

        assertThat("Ignore file should NOT be created for non-Skunkcrafts plugins",
                Files.exists(pluginFolder.resolve("skunkcrafts_updater_ignore.txt")), is(false));
    }

    @Test
    void trimDeletesEmptyPlatformFolder() throws Exception {
        // Create a plugin with only one Windows binary
        Path pluginFolder = tempDir.resolve("Resources/plugins/SingleBinary");
        Files.createDirectories(pluginFolder.resolve("win_x64"));
        Path windowsBinary = pluginFolder.resolve("win_x64/SingleBinary.xpl");
        createPeBinary(windowsBinary);

        Plugin plugin = new Plugin(new XPlane(tempDir), pluginFolder.resolve("win_x64/SingleBinary.xpl"));

        // Trim for Mac should remove the Windows binary and delete the empty folder
        plugin.trimToVariant(XPlaneVariant.MAC);

        assertThat("Windows binary should be removed", Files.notExists(windowsBinary));
        assertThat("Empty win_x64 folder should be deleted", Files.notExists(pluginFolder.resolve("win_x64")));
        assertThat("Base plugin folder should still exist", Files.exists(pluginFolder));
    }

    @Test
    void trimKeepsPlatformFolderWithOtherFiles() throws Exception {
        // Create a plugin with a Windows binary and a non-.xpl file in the same folder
        Path pluginFolder = tempDir.resolve("Resources/plugins/MixedContent");
        Path winFolder = pluginFolder.resolve("win_x64");
        Files.createDirectories(winFolder);
        Path windowsBinary = winFolder.resolve("MixedContent.xpl");
        Path otherFile = winFolder.resolve("README.txt");
        createPeBinary(windowsBinary);
        Files.writeString(otherFile, "This file should be preserved");

        Plugin plugin = new Plugin(new XPlane(tempDir), pluginFolder.resolve("win_x64/MixedContent.xpl"));

        // Trim for Mac should remove the Windows binary but keep the folder
        plugin.trimToVariant(XPlaneVariant.MAC);

        assertThat("Windows binary should be removed", Files.notExists(windowsBinary));
        assertThat("win_x64 folder should exist because it contains README.txt", Files.exists(winFolder));
        assertThat("Other file should be preserved", Files.exists(otherFile));
    }

    @Test
    void trimDeletesNestedEmptyDirectories() throws Exception {
        // Create a plugin with multiple platform folders at different nesting levels
        // Structure:
        // NestedEmpty/
        //   mac_x64/
        //     NestedEmpty.xpl (keep for Mac)
        //   win_x64/
        //     x64/
        //       NestedEmpty.xpl (remove for Mac)
        //   64/
        //     plugins/
        //       NestedEmpty.xpl (remove for Mac - PE binary)
        Path pluginFolder = tempDir.resolve("Resources/plugins/NestedEmpty");
        Path macFolder = pluginFolder.resolve("mac_x64");
        Path winFolder = pluginFolder.resolve("win_x64/x64");
        Path neutralFolder = pluginFolder.resolve("64/plugins");
        Files.createDirectories(macFolder);
        Files.createDirectories(winFolder);
        Files.createDirectories(neutralFolder);
        
        Path macBinary = macFolder.resolve("NestedEmpty.xpl");
        Path winBinary = winFolder.resolve("NestedEmpty.xpl");
        Path neutralBinary = neutralFolder.resolve("NestedEmpty.xpl");
        
        createMachOBinary(macBinary);
        createPeBinary(winBinary);
        createPeBinary(neutralBinary);

        Plugin plugin = new Plugin(new XPlane(tempDir), macBinary);

        // Trim for Mac should remove Windows binaries and clean up empty nested folders
        plugin.trimToVariant(XPlaneVariant.MAC);

        assertThat("Mac binary should remain", Files.exists(macBinary));
        assertThat("Windows binary should be removed", Files.notExists(winBinary));
        assertThat("Neutral PE binary should be removed", Files.notExists(neutralBinary));
        
        // Empty nested folders should be deleted
        assertThat("Empty win_x64/x64 folder should be deleted", Files.notExists(winFolder));
        assertThat("Empty win_x64 folder should be deleted", Files.notExists(pluginFolder.resolve("win_x64")));
        assertThat("Empty 64/plugins folder should be deleted", Files.notExists(neutralFolder));
        assertThat("Empty 64 folder should be deleted", Files.notExists(pluginFolder.resolve("64")));
        
        // mac_x64 should remain (contains Mac binary)
        assertThat("mac_x64 folder should exist", Files.exists(macFolder));
        
        // Base plugin folder should still exist
        assertThat("Base plugin folder should still exist", Files.exists(pluginFolder));
    }
}
