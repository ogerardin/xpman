package com.ogerardin.xplane.test.util.zip;

import com.ogerardin.xplane.util.zip.DryRunArchive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class DryRunArchiveTest {

    @TempDir
    Path tempDir;

    private Path target;
    private DryRunArchive dryRun;

    @BeforeEach
    void setUp() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            putEntry(zos, "root/", null);
            putEntry(zos, "root/file1.txt", "hello");
            putEntry(zos, "root/sub/", null);
            putEntry(zos, "root/sub/file2.txt", "world");
        }
        target = tempDir.resolve("target");
        Files.createDirectories(target);
        dryRun = new DryRunArchive(new ZipArchive(zipFile));
    }

    private static void putEntry(ZipOutputStream zos, String name, String content) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        if (content != null) {
            zos.write(content.getBytes(StandardCharsets.UTF_8));
        }
        zos.closeEntry();
    }

    private void existing(String relativePath) throws IOException {
        Path file = target.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, "old");
    }

    @Test
    void reportsOnlyExistingFiles() throws IOException {
        existing("root/file1.txt");

        dryRun.extract(target, null);

        assertThat(dryRun.overwrites(), contains(target.resolve("root/file1.txt")));
    }

    @Test
    void existingDirectoriesAreNotOverwrites() throws IOException {
        Files.createDirectories(target.resolve("root/sub"));

        dryRun.extract(target, null);

        assertThat(dryRun.overwrites(), is(empty()));
    }

    @Test
    void subpathPrefixIsStripped() throws IOException {
        existing("file1.txt");
        existing("root/file1.txt");

        dryRun.extract(target, Path.of("root"), null);

        assertThat(dryRun.overwrites(), contains(target.resolve("file1.txt")));
    }

    @Test
    void filterSelectsEntries() throws IOException {
        existing("root/file1.txt");
        existing("root/sub/file2.txt");

        dryRun.extract(target, path -> path.getFileName().toString().equals("file2.txt"), null);

        assertThat(dryRun.overwrites(), contains(target.resolve("root/sub/file2.txt")));
    }

    @Test
    void accumulatesAcrossExtractions() throws IOException {
        existing("root/file1.txt");
        existing("root/sub/file2.txt");

        dryRun.extract(target, path -> path.endsWith("file1.txt"), null);
        dryRun.extract(target, path -> path.endsWith("file2.txt"), null);

        assertThat(dryRun.overwrites(), containsInAnyOrder(
                target.resolve("root/file1.txt"), target.resolve("root/sub/file2.txt")));
    }

    @Test
    void writesNothing() throws IOException {
        dryRun.extract(target, null);

        try (var files = Files.list(target)) {
            assertThat(files.toList(), is(empty()));
        }
    }

    @Test
    void delegatesReadMethods() {
        assertThat(dryRun.isValidArchive(), is(true));
        assertThat(dryRun.entryCount(), is(4));
    }
}
