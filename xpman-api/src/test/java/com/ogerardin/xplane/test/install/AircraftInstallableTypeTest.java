package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.types.AircraftInstallableType;
import com.ogerardin.xplane.install.types.PluginInstallableType;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests recognition of aircraft archives by AircraftInstallableType,
 * including aircraft that bundle plugins.
 */
class AircraftInstallableTypeTest {

    @TempDir
    Path tempFolder;

    @Test
    void recognizesAircraftArchiveWithBundledPlugin() throws IOException {
        Path zip = createAircraftWithBundledPluginArchive();

        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(true));
        assertThat(new PluginInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void isDiscoveredAsAnInstallableType() throws IOException {
        Path zip = createAircraftWithBundledPluginArchive();

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
        assertThat(source.getInstallableType().get(), instanceOf(AircraftInstallableType.class));
    }

    @Test
    void recognizesPlainAircraftArchive() throws IOException {
        Path zip = createAircraftArchive();

        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void recognizesMultiAircraftPackArchive() throws IOException {
        Path zip = createMultiAircraftPackArchive();

        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeGenericPluginArchive() throws IOException {
        Path zip = createGenericPluginArchive();

        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void recognizesAircruiserShapeDeeplyNestedAcfWithAircraftLocalPlugin() throws IOException {
        // Aircruiser XP12 v1_3.zip: nested .acf + .xpl under plugins/ → aircraft, not plugin
        Path zip = createAircruiserArchive();

        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(true));
        assertThat(new PluginInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void recognizesPluginWithDemoAircraft() throws IOException {
        // Plugin ships a demo aircraft: root-level .xpl + .acf elsewhere → plugin, not aircraft
        Path zip = createPluginWithDemoAircraftArchive();

        assertThat(new PluginInstallableType().recognizes(new ZipArchive(zip)), is(true));
        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void recognizesPluginWhenMixedAircraftLocalAndStandaloneXpl() throws IOException {
        // .acf + bundled plugins/.xpl AND root-level .xpl → plugin wins (standalone wouldn't land correctly as aircraft)
        Path zip = createMixedArchive();

        assertThat(new PluginInstallableType().recognizes(new ZipArchive(zip)), is(true));
        assertThat(new AircraftInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void preconditionsFlagVersionMismatch() throws Exception {
        Path xp12Aircraft = createVersionedAircraftArchive("1200");
        var intoXp11 = new AircraftInstallableType()
                .preconditions(versionMock("11.55.1", XPlaneMajorVersion.XP11), new ZipArchive(xp12Aircraft));
        assertThat(intoXp11.stream().anyMatch(InspectionMessage::isError), is(true));

        Path xp11Aircraft = createVersionedAircraftArchive("1100");
        var intoXp12 = new AircraftInstallableType()
                .preconditions(versionMock("12.0.9", XPlaneMajorVersion.XP12), new ZipArchive(xp11Aircraft));
        assertThat(intoXp12.stream().noneMatch(InspectionMessage::isError), is(true));
        assertThat(intoXp12.stream().anyMatch(m ->
                m.getSeverity() == Severity.WARN && m.getMessage().contains("intended for")), is(true));
    }

    @Test
    void aircruiserArchiveInstallsToCorrectPath() throws Exception {
        // Aircruiser-shaped archive should install to Aircraft/Hangar 23/Aircruiser/, not double-nested
        Path zip = createAircruiserArchive();
        XPlane xplane = new XPlane(tempFolder);
        
        new AircraftInstallableType().install(xplane, new ZipArchive(zip), (_, _) -> {});
        
        Path expectedAcf = tempFolder.resolve("Aircraft/Hangar 23/Aircruiser/Aircruiser_XP12.acf");
        Path wrongAcf = tempFolder.resolve("Aircraft/Aircruiser/Aircraft/Hangar 23/Aircruiser/Aircruiser_XP12.acf");
        
        assertThat("Expected path should exist", Files.exists(expectedAcf), is(true));
        assertThat("Double-nested path should not exist", Files.exists(wrongAcf), is(false));
    }

    @Test
    void normalAircraftArchiveStillInstallsCorrectly() throws Exception {
        // Normal archive (no Aircraft/ element) should still extract under Aircraft/
        Path zip = createAircraftArchive();
        XPlane xplane = new XPlane(tempFolder);
        
        new AircraftInstallableType().install(xplane, new ZipArchive(zip), (_, _) -> {});
        
        Path expectedAcf = tempFolder.resolve("Aircraft/Jaguar/Jaguar_GR1.acf");
        assertThat("Normal aircraft should install under Aircraft/", Files.exists(expectedAcf), is(true));
    }

    @Test
    void overwriteDetectionWorksForAircruiserArchive() throws Exception {
        // Pre-create the expected location
        Path expectedLocation = tempFolder.resolve("Aircraft/Hangar 23/Aircruiser/Aircruiser_XP12.acf");
        Files.createDirectories(expectedLocation.getParent());
        Files.writeString(expectedLocation, "existing file");
        
        Path zip = createAircruiserArchive();
        XPlane xplane = new XPlane(tempFolder);
        
        var overwrites = xplane.getAircraftManager().overwrittenFiles(new ZipArchive(zip));
        
        assertThat("Should detect overwrite at expected location", 
                overwrites.stream().anyMatch(p -> p.equals(expectedLocation)), is(true));
    }

    // Helper methods to create test archives

    private Path createAircraftWithBundledPluginArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft-bundled-plugin", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Jaguar/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Jaguar/Jaguar_GR1.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Jaguar/plugins/xlua/64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Jaguar/plugins/xlua/64/mac.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createAircraftArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Jaguar/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Jaguar/Jaguar_GR1.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createMultiAircraftPackArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft-pack", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Pack/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Pack/Plane1/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Pack/Plane1/Plane1.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private Path createGenericPluginArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "generic-plugin", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("SomePlugin/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SomePlugin/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("SomePlugin/mac_x64/plugin.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Aircruiser-style archive: deeply nested .acf + .xpl under plugins/ */
    private Path createAircruiserArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircruiser", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Aircruiser/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Aircruiser/Aircraft/Hangar 23/Aircruiser/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Aircruiser/Aircraft/Hangar 23/Aircruiser/Aircruiser_XP12.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Aircruiser/Aircraft/Hangar 23/Aircruiser/plugins/Aircruiser_plugin/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Aircruiser/Aircraft/Hangar 23/Aircruiser/plugins/Aircruiser_plugin/mac_x64/Aircruiser_plugin.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Plugin archive that ships a demo aircraft: root-level .xpl + .acf elsewhere */
    private Path createPluginWithDemoAircraftArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "plugin-with-demo", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("PluginWithDemo/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("PluginWithDemo/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("PluginWithDemo/mac_x64/plugin.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("PluginWithDemo/demo/DemoAircraft/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("PluginWithDemo/demo/DemoAircraft/Demo.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Mixed archive: .acf + bundled plugins/.xpl AND root-level .xpl */
    private Path createMixedArchive() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "mixed", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Mixed/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Mixed/Mixed.acf"));
            out.write("dummy acf content".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Mixed/plugins/bundled/mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Mixed/plugins/bundled/mac_x64/bundled.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Mixed/Mac_x64/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Mixed/Mac_x64/standalone.xpl"));
            out.write("dummy xpl content".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Single-root aircraft archive with a parseable ACF of the given spec version. */
    private Path createVersionedAircraftArchive(String spec) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("TestAircraft/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("TestAircraft/TestAircraft.acf"));
            out.write(acfContent(spec).getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Minimal ACF accepted by AcfFileParser: origin / "NNNN Version" / "ACF" / blank / properties block. */
    private static String acfContent(String specVersion) {
        return """
                I
                %s Version
                ACF
                
                PROPERTIES_BEGIN
                P acf/_name Test Aircraft
                PROPERTIES_END
                """.formatted(specVersion);
    }

    /**
     * A mocked X-Plane reporting the given version, delegating paths and managers to a real
     * temp install — the AssertXPlaneVersionAtLeastTest idiom, extended for the
     * aircraft-manager-aware preconditions chain.
     */
    private XPlane versionMock(String version, XPlaneMajorVersion major) throws Exception {
        XPlane real = new XPlane(tempFolder);
        XPlane xPlane = mock(XPlane.class);
        when(xPlane.getVersion()).thenReturn(version);
        when(xPlane.getMajorVersion()).thenReturn(major);
        when(xPlane.getPaths()).thenReturn(real.getPaths());
        when(xPlane.getAircraftManager()).thenReturn(real.getAircraftManager());
        return xPlane;
    }
}
