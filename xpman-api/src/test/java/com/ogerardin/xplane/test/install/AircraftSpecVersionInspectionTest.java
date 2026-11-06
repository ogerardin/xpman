package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.install.inspections.AircraftSpecVersionInspection;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link AircraftSpecVersionInspection}: ACF spec version vs target X-Plane version at install time.
 */
class AircraftSpecVersionInspectionTest {

    @TempDir
    Path tempFolder;

    @Test
    void newerAcfBlocksAsError() throws IOException {
        var result = inspect(acfZip("1200"), versionMock("11.55.1", XPlaneMajorVersion.XP11));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(result.get(0).getMessage(), both(containsString("X-Plane 12")).and(containsString("will not work")));
    }

    @Test
    void olderAcfWarns() throws IOException {
        var result = inspect(acfZip("1100"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
        assertThat(result.get(0).getMessage(), containsString("X-Plane 11"));
    }

    @Test
    void matchingSpecIsSilent() throws IOException {
        var result = inspect(acfZip("1200"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(0));
    }

    @Test
    void undetectableSimVersionIsSkipped() throws IOException {
        var result = inspect(acfZip("1200"), versionMock(null, null));
        assertThat(result.size(), is(0));
    }

    @Test
    void unparseableAcfEntriesAreSkippedIndividually() throws IOException {
        // one garbage ACF must not suppress the check of the valid one
        Path zip = zipOf(Map.of(
                "TestAircraft/Broken.acf", "dummy acf content",
                "TestAircraft/Good.acf", acfContent("1100")));
        var result = inspect(zip, versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
        assertThat(result.get(0).getDetails(), containsString("Good.acf"));
    }

    @Test
    void legacySpecComparesNumerically() throws IOException {
        // lexicographic compare would rank "900" above "1200" and wrongly produce an error
        var result = inspect(acfZip("900"), versionMock("12.0.9", XPlaneMajorVersion.XP12));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.WARN));
    }

    @Test
    void mixedSpecsYieldSingleWorstCaseMessage() throws IOException {
        Path zip = zipOf(Map.of(
                "TestAircraft/VariantA.acf", acfContent("1100"),
                "TestAircraft/VariantB.acf", acfContent("1200")));
        var result = inspect(zip, versionMock("11.55.1", XPlaneMajorVersion.XP11));
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
        assertThat(result.get(0).getDetails(), both(containsString("VariantA.acf")).and(containsString("VariantB.acf")));
    }

    private InspectionResult inspect(Path zip, XPlane xPlane) throws IOException {
        return new AircraftSpecVersionInspection(xPlane).inspect(new ZipArchive(zip));
    }

    private Path acfZip(String spec) throws IOException {
        return zipOf(Map.of("TestAircraft/TestAircraft.acf", acfContent(spec)));
    }

    private Path zipOf(Map<String, String> entries) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "aircraft", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (var entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes());
                out.closeEntry();
            }
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

    private XPlane versionMock(String version, XPlaneMajorVersion major) {
        XPlane xPlane = mock(XPlane.class);
        when(xPlane.getVersion()).thenReturn(version);
        when(xPlane.getMajorVersion()).thenReturn(major);
        return xPlane;
    }
}
