package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.aircraft.custom.CalibraA330;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
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
 * Recognition and preconditions of the Calibra Simulations A330-200 installable type.
 */
class CalibraA330InstallableTypeTest {

    @TempDir
    Path tempFolder;

    // recognition

    @Test
    void recognizesCalibraA330Archive() throws IOException {
        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        assertThat(new CalibraA330.CalibraA330InstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeGenericA330Archive() throws IOException {
        Path zip = createA330ArchiveWithoutMarker();

        assertThat(new CalibraA330.CalibraA330InstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void doesNotRecognizeOtherCalibraAircraft() throws IOException {
        // A hypothetical future Calibra A340-600 must not be misrecognized as the A330-200
        Path zip = createCalibraArchiveWithVersionText("Some Other Calibra Aircraft\nversion 1.0.0\n");

        assertThat(new CalibraA330.CalibraA330InstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void genericAircraftTypeStillRecognizesCalibraArchive() throws IOException {
        // the Calibra type extends the generic aircraft type; super.recognizes() must stay true
        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        assertThat(new com.ogerardin.xplane.install.types.AircraftInstallableType()
                .recognizes(new ZipArchive(zip)), is(true));
    }

    // type detection priority

    @Test
    void winsTypeDetectionOverGenericAircraft() throws IOException {
        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        var type = ArchiveInstallSource.ofZip(zip).getInstallableType();

        assertThat(type.isPresent(), is(true));
        assertThat(type.get(), instanceOf(CalibraA330.CalibraA330InstallableType.class));
        assertThat(type.get().description(), is("Calibra Simulations A330-200"));
    }

    // preconditions — sim-version minimum

    @Test
    void preconditionsWarnWhenSimTooOld() throws Exception {
        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        var result = new CalibraA330.CalibraA330InstallableType()
                .preconditions(versionMock("12.4.3", XPlaneMajorVersion.XP12), new ZipArchive(zip));

        assertThat(result.stream().anyMatch(m ->
                        !m.isAbort() && m.getSeverity() == Severity.WARN
                                && m.getMessage().contains("will probably not work")),
                is(true));
    }

    @Test
    void preconditionsCleanWhenSimMeetsMinimum() throws Exception {
        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        var result = new CalibraA330.CalibraA330InstallableType()
                .preconditions(versionMock("12.4.4", XPlaneMajorVersion.XP12), new ZipArchive(zip));

        assertThat(result.stream().anyMatch(InspectionMessage::isAbort), is(false));
    }

    @Test
    void preconditionsSkipWhenSimVersionUndetectable() throws Exception {
        // Mock XPlane with no probed version → install should not be blocked.
        // Back paths/managers with a fresh real install; avoid touching getMajorVersion on it,
        // because its lazy init would NPE on an empty temp folder.
        XPlane real = new XPlane(tempFolder);
        XPlane mock = mock(XPlane.class);
        when(mock.getVersion()).thenReturn(null);
        when(mock.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);
        when(mock.getPaths()).thenReturn(real.getPaths());
        when(mock.getAircraftManager()).thenReturn(real.getAircraftManager());

        Path zip = createCalibraArchive("1.0.0-beta.3", "A330-200 by Calibra Simulations");

        var result = new CalibraA330.CalibraA330InstallableType()
                .preconditions(mock, new ZipArchive(zip));

        assertThat(result.stream().filter(m -> m.getMessage() != null && m.getMessage().contains("will probably not work"))
                .count(), is(0L));
    }

    // helpers

    /**
     * Calibra-shaped archive: root folder containing the .acf and a sibling version.txt with
     * the "A330-200 by Calibra Simulations" marker.
     */
    private Path createCalibraArchive(String version, String nameLine) throws IOException {
        return createCalibraArchiveWithVersionText(nameLine + "\nversion " + version + "\n");
    }

    /** Same shape but with the exact version.txt content passed in. */
    private Path createCalibraArchiveWithVersionText(String versionTxtContent) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "calibra-a330", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Calibra A330-200/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Calibra A330-200/A330-200.acf"));
            out.write(calibraAcf().getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Calibra A330-200/version.txt"));
            out.write(versionTxtContent.getBytes());
            out.closeEntry();
        }
        return zip;
    }

    /** Same shape but version.txt has no Calibra marker — falls through to generic aircraft. */
    private Path createA330ArchiveWithoutMarker() throws IOException {
        Path zip = Files.createTempFile(tempFolder, "generic-a330", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("Generic A330/"));
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Generic A330/A330-300.acf"));
            out.write(calibraAcf().getBytes());
            out.closeEntry();

            out.putNextEntry(new ZipEntry("Generic A330/version.txt"));
            out.write("Some other aircraft\nversion 1.0.0\n".getBytes());
            out.closeEntry();
        }
        return zip;
    }

    private static String calibraAcf() {
        return """
                I
                1200 version
                ACF

                PROPERTIES_BEGIN
                P acf/_name Airbus A330-200
                P acf/_studio Laminar Research
                P acf/_author Alex Unruh; A330-200: Calibra Simulations
                P acf/_version 1.0.0
                PROPERTIES_END
                """;
    }

    /**
     * A mocked X-Plane reporting the given version, delegating paths and managers to a real
     * temp install — mirrors the {@code AircraftInstallableTypeTest#versionMock} idiom so
     * preconditions run through the full inspection chain.
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
