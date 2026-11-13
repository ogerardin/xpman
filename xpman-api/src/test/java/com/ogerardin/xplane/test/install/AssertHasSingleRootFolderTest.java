package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.inspections.AssertHasSingleRootFolder;
import com.ogerardin.xplane.util.zip.ZipArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class AssertHasSingleRootFolderTest {

    @TempDir
    Path tempDir;

    @Test
    void archiveWithOneRootFolderAndRootLevelFilePasses() throws IOException {
        // LST Package shape: "Living Scenery Technology/" folder + "Manual.pdf" root file
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("Living Scenery Technology/"));
            out.putNextEntry(new ZipEntry("Living Scenery Technology/mac.xpl"));
            out.putNextEntry(new ZipEntry("Manual.pdf"));
        }

        InspectionResult result = AssertHasSingleRootFolder.INSTANCE.inspect(new ZipArchive(zipFile));

        // Should pass - no error about single root folder
        assertThat(result.stream().noneMatch(msg -> msg.getMessage().contains("single root folder")), is(true));
        // Should have info message with root folder name
        assertThat(result.stream().anyMatch(msg -> msg.getMessage().contains("Living Scenery Technology")), is(true));
    }

    @Test
    void archiveWithOneRootFolderPasses() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("MyPlugin/"));
            out.putNextEntry(new ZipEntry("MyPlugin/mac.xpl"));
        }

        InspectionResult result = AssertHasSingleRootFolder.INSTANCE.inspect(new ZipArchive(zipFile));

        // Should pass - no error about single root folder
        assertThat(result.stream().noneMatch(msg -> msg.getMessage().contains("single root folder")), is(true));
    }

    @Test
    void archiveWithTwoRootFoldersFails() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("Folder1/"));
            out.putNextEntry(new ZipEntry("Folder1/file.txt"));
            out.putNextEntry(new ZipEntry("Folder2/"));
            out.putNextEntry(new ZipEntry("Folder2/file.txt"));
        }

        InspectionResult result = AssertHasSingleRootFolder.INSTANCE.inspect(new ZipArchive(zipFile));

        assertThat("Archive with two root folders should fail",
                result.stream().anyMatch(msg -> msg.getMessage().contains("single root folder")));
    }

    @Test
    void archiveWithOnlyRootFilesFails() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("file1.txt"));
            out.putNextEntry(new ZipEntry("file2.txt"));
        }

        InspectionResult result = AssertHasSingleRootFolder.INSTANCE.inspect(new ZipArchive(zipFile));

        assertThat("Archive with only root files should fail",
                result.stream().anyMatch(msg -> msg.getMessage().contains("single root folder")));
    }

    @Test
    void archiveWithEmptyRootFolderFails() throws IOException {
        Path zipFile = tempDir.resolve("test.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            out.putNextEntry(new ZipEntry("EmptyFolder/"));
        }

        InspectionResult result = AssertHasSingleRootFolder.INSTANCE.inspect(new ZipArchive(zipFile));

        // Empty folder has no entries with nameCount > 1, so it fails
        assertThat("Archive with empty root folder should fail",
                result.stream().anyMatch(msg -> msg.getMessage().contains("single root folder")));
    }
}
