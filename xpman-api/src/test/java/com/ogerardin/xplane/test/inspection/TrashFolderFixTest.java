package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.inspection.TrashFolderFix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TrashFolderFix}.
 */
class TrashFolderFixTest {

    @TempDir
    Path tempDir;

    @Test
    void movesExistingFolderToTrash() throws IOException {
        Path folder = tempDir.resolve("test-folder");
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("file.txt"), "content");

        TrashFolderFix fix = new TrashFolderFix(folder) {
            @Override
            protected void moveToTrash(Path f) throws IOException {
                assertEquals(folder, f);
                // Simulate trash by deleting
                Files.deleteIfExists(folder.resolve("file.txt"));
                Files.delete(folder);
            }
        };
        fix.apply();

        assertFalse(Files.exists(folder));
    }

    @Test
    void skipsMissingFolderSilently() {
        Path missing = tempDir.resolve("missing-folder");

        TrashFolderFix fix = new TrashFolderFix(missing) {
            @Override
            protected void moveToTrash(Path f) {
                fail("Should not be called for missing folder");
            }
        };
        fix.apply();
    }

    @Test
    void labelIncludesFolderName() throws IOException {
        Path folder = tempDir.resolve("my-livery");
        Files.createDirectories(folder);

        TrashFolderFix fix = new TrashFolderFix(folder);

        assertEquals("remove livery", fix.label());
    }

    @Test
    void throwsUncheckedIOExceptionOnFailure() throws IOException {
        Path folder = tempDir.resolve("test-folder");
        Files.createDirectories(folder);

        TrashFolderFix fix = new TrashFolderFix(folder) {
            @Override
            protected void moveToTrash(Path f) throws IOException {
                throw new IOException("Simulated failure");
            }
        };

        assertThrows(java.io.UncheckedIOException.class, fix::apply);
    }
}
