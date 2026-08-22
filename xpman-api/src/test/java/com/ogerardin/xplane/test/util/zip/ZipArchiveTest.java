package com.ogerardin.xplane.test.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipArchiveTest {

    @TempDir
    Path tempDir;

    private Path zipFile;

    @BeforeEach
    void setUp() throws IOException {
        zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            putEntry(zos, "root/", null);
            putEntry(zos, "root/file1.txt", "hello");
            putEntry(zos, "root/sub/", null);
            putEntry(zos, "root/sub/file2.txt", "world");
        }
    }

    private static void putEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        if (content != null) {
            zos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        zos.closeEntry();
    }

    private ZipArchive newArchive() {
        return new ZipArchive(zipFile);
    }

    @Test
    void testIsValidArchive() throws IOException {
        assertThat(newArchive().isValidArchive(), is(true));

        final Path notAZip = tempDir.resolve("not-a-zip.txt");
        Files.writeString(notAZip, "this is not a zip file");
        assertThat(new ZipArchive(notAZip).isValidArchive(), is(false));
    }

    @Test
    void testEntryCount() {
        assertThat(newArchive().entryCount(), is(4));
    }

    @Test
    void testExtract() throws IOException {
        Path target = tempDir.resolve("target");
        newArchive().extract(target, null);

        assertThat(Files.exists(target.resolve("root/file1.txt")), is(true));
        assertThat(Files.exists(target.resolve("root/sub/file2.txt")), is(true));
        assertThat(Files.readString(target.resolve("root/file1.txt")), is("hello"));
        assertThat(Files.readString(target.resolve("root/sub/file2.txt")), is("world"));
    }

    @Test
    void testExtractWithProgress() throws IOException {
        Path target = tempDir.resolve("target");
        List<Double> ratios = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        ProgressListener progressListener = (ratio, message) -> {
            if (ratio != null) ratios.add(ratio);
            if (message != null) messages.add(message);
        };

        newArchive().extract(target, progressListener);

        assertThat(Files.exists(target.resolve("root/sub/file2.txt")), is(true));
        assertThat(ratios, hasSize(greaterThan(0)));
        assertThat(ratios.get(ratios.size() - 1), is(closeTo(1.0, 0.0001)));
        assertThat(messages, hasItem(startsWith("Extracting ")));
    }

    @Test
    void testGetPaths() {
        assertThat(newArchive().getPaths(), hasItems(
                Path.of("root/"),
                Path.of("root/file1.txt"),
                Path.of("root/sub/file2.txt")
        ));
    }

    @Test
    void testGetAsText() throws IOException {
        assertThat(newArchive().getAsText(Path.of("root/sub/file2.txt")), is("world"));
    }

    @Test
    void testGetAsTextNotFound() {
        assertThrows(java.io.FileNotFoundException.class,
                () -> newArchive().getAsText(Path.of("no/such/file.txt")));
    }
}
