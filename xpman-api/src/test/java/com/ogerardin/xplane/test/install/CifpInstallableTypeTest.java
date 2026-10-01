package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.types.CifpInstallableType;
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
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Tests recognition and installation of FAA CIFP cycle archives by CifpInstallableType.
 */
class CifpInstallableTypeTest {

    private static final String CONTENT = "HDR01FAACIFP18      001P013203969192610\n";

    private static final ProgressListener NO_PROGRESS = (ratio, message) -> {};

    @TempDir
    Path tempFolder;

    @Test
    void recognizesArchiveContainingFaacifp18() throws IOException {
        Path zip = zipNamed("FAACIFP18");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void recognizesArchiveContainingNestedFaacifp18() throws IOException {
        Path zip = zipNamed("CIFP/2026-09-30/FAACIFP18");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(true));
    }

    @Test
    void doesNotRecognizeNavDataArchive() throws IOException {
        Path zip = zipNamed("earth_424.dat");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void doesNotRecognizeArchiveContainingDifferentFile() throws IOException {
        Path zip = zipNamed("FAACIFP18.dat");

        assertThat(new CifpInstallableType().recognizes(new ZipArchive(zip)), is(false));
    }

    @Test
    void isDiscoveredAsAnInstallableType() throws IOException {
        Path zip = zipNamed("FAACIFP18");

        ArchiveInstallSource source = ArchiveInstallSource.ofZip(zip);

        assertThat(source.getInstallableType().isPresent(), is(true));
    }

    @Test
    void installsSourceAsEarth424() throws Exception {
        Path zip = zipNamed("FAACIFP18");
        XPlane xPlane = new XPlane(tempFolder);

        new CifpInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path installed = xPlane.getPaths().customData().resolve("earth_424.dat");
        assertThat(Files.exists(installed), is(true));
        assertThat(Files.readString(installed), is(CONTENT));
    }

    @Test
    void installsNestedSourceAsEarth424() throws Exception {
        Path zip = zipNamed("CIFP/2026-09-30/FAACIFP18");
        XPlane xPlane = new XPlane(tempFolder);

        new CifpInstallableType().install(xPlane, new ZipArchive(zip), NO_PROGRESS);

        Path installed = xPlane.getPaths().customData().resolve("earth_424.dat");
        assertThat(Files.readString(installed), is(CONTENT));
        // the nested folders of the source entry must not leak into Custom Data
        assertThat(Files.exists(xPlane.getPaths().customData().resolve("CIFP")), is(false));
    }

    @Test
    void warnsThatOnlyUsNavDataRemainsAvailable() throws Exception {
        Path zip = zipNamed("FAACIFP18");

        InspectionResult result = new CifpInstallableType().preconditions(new XPlane(tempFolder), new ZipArchive(zip));

        assertThat(result.getMessages(), hasSize(1));
        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", containsString("Only US navdata will be available")))));
    }

    /**
     * Creates a zip whose single entry has the specified name and dummy content.
     */
    private Path zipNamed(String entryName) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "test", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write(CONTENT.getBytes());
            out.closeEntry();
        }
        return zip;
    }
}