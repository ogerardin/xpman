package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class InstallTargetTest {

    @TempDir
    Path tempDir;

    private Path folder;
    private Archive archive;
    private final List<String> calls = new ArrayList<>();

    private final InstallTarget target = new InstallTarget() {
        @Override
        public void unpack(Archive archive, ProgressListener progressListener) throws IOException {
            calls.add("unpack");
            archive.extract(folder, progressListener);
        }

        @Override
        public void reload() {
            calls.add("reload");
        }
    };

    @BeforeEach
    void setUp() throws IOException {
        Path zip = tempDir.resolve("addon.zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
            zos.putNextEntry(new ZipEntry("addon/readme.txt"));
            zos.write("new".getBytes());
            zos.closeEntry();
        }
        archive = new ZipArchive(zip);
        folder = tempDir.resolve("xplane");
        Files.createDirectories(folder);
    }

    @Test
    void installExtractsThenReloads() throws IOException {
        target.install(archive, (ratio, message) -> {});

        assertThat(calls, contains("unpack", "reload"));
        assertThat(Files.readString(folder.resolve("addon/readme.txt")), is("new"));
    }

    @Test
    void overwrittenFilesReportsExistingFilesWithoutWritingOrReloading() throws IOException {
        Files.createDirectories(folder.resolve("addon"));
        Files.writeString(folder.resolve("addon/readme.txt"), "old");

        List<Path> overwrites = target.overwrittenFiles(archive);

        assertThat(overwrites, contains(folder.resolve("addon/readme.txt")));
        assertThat(Files.readString(folder.resolve("addon/readme.txt")), is("old"));
        assertThat(calls, contains("unpack"));
    }

    @Test
    void overwrittenFilesIsEmptyOnFreshInstall() throws IOException {
        assertThat(target.overwrittenFiles(archive), is(empty()));
        assertThat(Files.list(folder).toList(), is(empty()));
    }
}
