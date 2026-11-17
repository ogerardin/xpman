package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests for aircraft-local plugin trimming functionality.
 */
class AircraftPluginTrimTest {

    @TempDir
    Path tempDir;

    /**
     * Creates an aircraft folder structure with:
     * - Aircraft.acf
     * - plugins/xlua/{mac_x64,win_x64,lin_x64}/xlua.xpl
     * - skunkcrafts_updater.cfg (optional)
     */
    private Path createAircraftWithPlugins(String aircraftName, boolean withSkunkcrafts) throws IOException {
        Path aircraftFolder = tempDir.resolve(aircraftName);
        Files.createDirectories(aircraftFolder);

        // Create a minimal ACF file
        Path acfFile = aircraftFolder.resolve(aircraftName + ".acf");
        Files.writeString(acfFile, "AIRCRAFT\n");

        // Create plugins/xlua folder
        Path pluginsFolder = aircraftFolder.resolve("plugins/xlua");
        Files.createDirectories(pluginsFolder);

        // Create platform-specific binaries with correct magic bytes
        Path macX64 = pluginsFolder.resolve("mac_x64");
        Path winX64 = pluginsFolder.resolve("win_x64");
        Path linX64 = pluginsFolder.resolve("lin_x64");
        Files.createDirectories(macX64);
        Files.createDirectories(winX64);
        Files.createDirectories(linX64);

        createMachOBinary(macX64.resolve("xlua.xpl"));
        createPeBinary(winX64.resolve("xlua.xpl"));
        createElfBinary(linX64.resolve("xlua.xpl"));

        // Optionally create skunkcrafts config
        if (withSkunkcrafts) {
            Path skunkcraftsConfig = aircraftFolder.resolve("skunkcrafts_updater.cfg");
            Files.writeString(skunkcraftsConfig, "name|" + aircraftName + "\nversion|1.0.0\n");
        }

        return aircraftFolder;
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
    void trimPluginBinariesForMacRemovesNonMacBinaries() throws Exception {
        // Create aircraft with plugins (no skunkcrafts)
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", false);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        // Verify initial state
        List<Path> allXpls = aircraft.getNonApplicablePlatformBinaries(XPlaneVariant.MAC);
        assertThat("Should have 2 non-applicable .xpl files initially", allXpls.size(), is(2));

        // Trim to MAC variant
        List<Path> removed = aircraft.trimPluginBinaries(XPlaneVariant.MAC);

        // Verify removed items
        assertThat("Should remove 2 binaries for other platforms", removed.size(), is(2));

        // Verify what remains
        List<Path> remainingXpls = aircraft.getNonApplicablePlatformBinaries(XPlaneVariant.MAC);
        assertThat("Should have no non-applicable .xpl files remaining", remainingXpls.size(), is(0));

        // Check specific files/folders
        Path pluginsFolder = aircraftFolder.resolve("plugins/xlua");
        assertThat("mac_x64 folder should exist", Files.exists(pluginsFolder.resolve("mac_x64")));
        assertThat("mac_x64 binary should exist", Files.exists(pluginsFolder.resolve("mac_x64/xlua.xpl")));
        // Empty platform folders should be deleted
        assertThat("Empty win_x64 folder should be deleted", Files.notExists(pluginsFolder.resolve("win_x64")));
        assertThat("Empty lin_x64 folder should be deleted", Files.notExists(pluginsFolder.resolve("lin_x64")));

        // Verify no skunkcrafts ignore file was created
        Path ignoreFile = aircraftFolder.resolve("skunkcrafts_updater_ignore.txt");
        assertThat("skunkcrafts_updater_ignore.txt should not exist", Files.notExists(ignoreFile));
    }

    @Test
    void trimPluginBinariesWithSkunkcraftsCreatesIgnoreFile() throws Exception {
        // Create aircraft with plugins and skunkcrafts
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", true);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        // Verify skunkcrafts is detected
        assertThat("Aircraft should be skunkcrafts-managed", aircraft.isSkunkcraftsManaged());

        // Trim to MAC variant
        List<Path> removed = aircraft.trimPluginBinaries(XPlaneVariant.MAC);

        assertThat("Should remove 2 binaries for other platforms", removed.size(), is(2));

        // Verify skunkcrafts ignore file was created
        Path ignoreFile = aircraftFolder.resolve("skunkcrafts_updater_ignore.txt");
        assertThat("skunkcrafts_updater_ignore.txt should exist", Files.exists(ignoreFile));

        // Verify ignore file content
        String ignoreContent = Files.readString(ignoreFile);
        assertThat("Ignore file should contain win_x64/xlua.xpl", ignoreContent, containsString("plugins/xlua/win_x64/xlua.xpl"));
        assertThat("Ignore file should contain lin_x64/xlua.xpl", ignoreContent, containsString("plugins/xlua/lin_x64/xlua.xpl"));
    }

    @Test
    void trimPluginBinariesForWindowsRemovesNonWindowsBinaries() throws Exception {
        // Create aircraft with plugins (no skunkcrafts)
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", false);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        // Trim to WINDOWS variant
        List<Path> removed = aircraft.trimPluginBinaries(XPlaneVariant.WINDOWS);

        assertThat("Should remove 2 binaries for other platforms", removed.size(), is(2));

        // Verify what remains
        Path pluginsFolder = aircraftFolder.resolve("plugins/xlua");
        assertThat("win_x64 binary should exist", Files.exists(pluginsFolder.resolve("win_x64/xlua.xpl")));
        assertThat("mac_x64 binary should be removed", Files.notExists(pluginsFolder.resolve("mac_x64/xlua.xpl")));
        assertThat("lin_x64 binary should be removed", Files.notExists(pluginsFolder.resolve("lin_x64/xlua.xpl")));
    }

    @Test
    void trimPluginBinariesForLinuxRemovesNonLinuxBinaries() throws Exception {
        // Create aircraft with plugins (no skunkcrafts)
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", false);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        // Trim to LINUX variant
        List<Path> removed = aircraft.trimPluginBinaries(XPlaneVariant.LINUX);

        assertThat("Should remove 2 binaries for other platforms", removed.size(), is(2));

        // Verify what remains
        Path pluginsFolder = aircraftFolder.resolve("plugins/xlua");
        assertThat("lin_x64 binary should exist", Files.exists(pluginsFolder.resolve("lin_x64/xlua.xpl")));
        assertThat("mac_x64 binary should be removed", Files.notExists(pluginsFolder.resolve("mac_x64/xlua.xpl")));
        assertThat("win_x64 binary should be removed", Files.notExists(pluginsFolder.resolve("win_x64/xlua.xpl")));
    }

    @Test
    void getNonApplicablePlatformBinariesReturnsCorrectFiles() throws Exception {
        // Create aircraft with plugins
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", false);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        List<Path> nonApplicable = aircraft.getNonApplicablePlatformBinaries(XPlaneVariant.MAC);

        assertThat("Should have 2 non-applicable .xpl files", nonApplicable.size(), is(2));

        // Check that the list includes the correct files (by checking parent folder names)
        List<String> nonApplicableFolders = nonApplicable.stream()
                .map(Path::getParent)
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();

        assertThat("Should not include the applicable mac_x64 folder", nonApplicableFolders, not(hasItem("mac_x64")));
        assertThat("Should include win_x64 folder", nonApplicableFolders, hasItem("win_x64"));
        assertThat("Should include lin_x64 folder", nonApplicableFolders, hasItem("lin_x64"));
    }

    @Test
    void isSkunkcraftsManagedReturnsTrueWhenConfigExists() throws Exception {
        // Create aircraft with skunkcrafts
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", true);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        assertThat("Aircraft should be skunkcrafts-managed", aircraft.isSkunkcraftsManaged());
    }

    @Test
    void isSkunkcraftsManagedReturnsFalseWhenConfigDoesNotExist() throws Exception {
        // Create aircraft without skunkcrafts
        Path aircraftFolder = createAircraftWithPlugins("TestAircraft", false);
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        assertThat("Aircraft should not be skunkcrafts-managed", !aircraft.isSkunkcraftsManaged());
    }

    @Test
    void trimPluginBinariesRemovesNonApplicableNativeLibraries() throws Exception {
        // Create aircraft with native libraries in a plugin folder
        Path aircraftFolder = tempDir.resolve("TestAircraft");
        Files.createDirectories(aircraftFolder);

        // Create a minimal ACF file
        Path acfFile = aircraftFolder.resolve("TestAircraft.acf");
        Files.writeString(acfFile, "AIRCRAFT\n");

        // Create plugin folder with native libraries for all platforms
        Path pluginFolder = aircraftFolder.resolve("plugins/myplugin");
        Files.createDirectories(pluginFolder);

        Path macLib = pluginFolder.resolve("libpdfium.dylib");
        Path winLib = pluginFolder.resolve("pdfium.dll");
        Path linLib = pluginFolder.resolve("libpdfium.so");
        Path txtFile = pluginFolder.resolve("LICENSE.txt");

        Files.writeString(macLib, "fake dylib");
        Files.writeString(winLib, "fake dll");
        Files.writeString(linLib, "fake so");
        Files.writeString(txtFile, "license text");

        XPlane xplane = new XPlane(tempDir);
        Aircraft aircraft = new Aircraft(xplane, new AcfFile(acfFile));

        // Verify all files exist initially
        assertThat("macOS lib should exist", Files.exists(macLib));
        assertThat("Windows lib should exist", Files.exists(winLib));
        assertThat("Linux lib should exist", Files.exists(linLib));
        assertThat("License file should exist", Files.exists(txtFile));

        // Verify detection works correctly
        List<Path> nonApplicable = aircraft.getNonApplicablePlatformBinaries(XPlaneVariant.MAC);
        assertThat("Should have 2 non-applicable native libraries", nonApplicable.size(), is(2));
        assertThat("Should include Windows lib", nonApplicable, hasItem(winLib));
        assertThat("Should include Linux lib", nonApplicable, hasItem(linLib));
        assertThat("Should not include macOS lib", nonApplicable, not(hasItem(macLib)));
        assertThat("Should not include text file", nonApplicable, not(hasItem(txtFile)));

        // Trim to macOS
        List<Path> removed = aircraft.trimPluginBinaries(XPlaneVariant.MAC);

        // Verify removal
        assertThat("Should remove 2 native libraries", removed.size(), is(2));
        assertThat("macOS lib should still exist", Files.exists(macLib));
        assertThat("Windows lib should be removed", Files.notExists(winLib));
        assertThat("Linux lib should be removed", Files.notExists(linLib));
        assertThat("License file should still exist", Files.exists(txtFile));
    }
}
