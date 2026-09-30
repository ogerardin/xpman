package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.types.CifpInstallableType;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests recognition of FAA CIFP cycle archives by CifpInstallableType.
 */
class CifpInstallableTypeTest {

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

    /**
     * Creates a zip whose single entry has the specified name and dummy content.
     */
    private Path zipNamed(String entryName) throws IOException {
        Path zip = Files.createTempFile(tempFolder, "test", ".zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write("test content".getBytes());
            out.closeEntry();
        }
        return zip;
    }
}