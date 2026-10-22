package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.inspection.TrashFileFix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/** Tests for {@link TrashFileFix}. */
class TrashFileFixTest {

    @Test
    void applyMovesFileToTrashAndRunsCallback(@TempDir Path tempDir) throws IOException {
        Path file = Files.writeString(tempDir.resolve("test.txt"), "content");
        AtomicReference<Path> trashedPath = new AtomicReference<>();
        AtomicBoolean callbackRan = new AtomicBoolean(false);

        TrashFileFix fix = new TrashFileFix(file, () -> callbackRan.set(true)) {
            @Override
            protected void moveToTrash(Path path) {
                trashedPath.set(path);
            }
        };

        fix.apply();

        assertThat("trashes the file", trashedPath.get(), is(file));
        assertThat("runs the callback", callbackRan.get(), is(true));
    }

    @Test
    void applyIsNoopWhenFileDoesNotExist(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("missing.txt");
        AtomicBoolean callbackRan = new AtomicBoolean(false);

        TrashFileFix fix = new TrashFileFix(missing, () -> callbackRan.set(true)) {
            @Override
            protected void moveToTrash(Path path) {
                throw new AssertionError("should not be called");
            }
        };

        fix.apply();

        assertThat("callback not run", callbackRan.get(), is(false));
    }

    @Test
    void labelUsesFileName() {
        Path file = Path.of("/some/path/FAACIFP18");
        TrashFileFix fix = new TrashFileFix(file, () -> {}) {
            @Override
            protected void moveToTrash(Path path) {}
        };

        assertThat(fix.label(), is("Move FAACIFP18 to Trash"));
    }

    @Test
    void labelHandlesNullFileName() {
        TrashFileFix fix = new TrashFileFix(Path.of("/"), () -> {}) {
            @Override
            protected void moveToTrash(Path path) {}
        };

        assertThat(fix.label(), is("Move / to Trash"));
    }
}
