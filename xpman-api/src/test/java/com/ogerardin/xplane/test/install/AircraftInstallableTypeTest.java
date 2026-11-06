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
