package com.ogerardin.xplane.inspection;

import com.sun.jna.platform.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Fix} that moves a single file to the OS trash, then runs a post-action callback.
 *
 * <p>The trash call is wrapped in an overridable method so tests can stub it without touching
 * the real Trash. Missing files are a no-op, so a double click is harmless. IO failures are
 * rethrown as {@link UncheckedIOException} so the UI can catch them in one place.</p>
 */
public class TrashFileFix implements Fix {

    private final Path file;
    private final Runnable afterApply;

    public TrashFileFix(Path file, Runnable afterApply) {
        this.file = file;
        this.afterApply = afterApply;
    }

    @Override
    public String label() {
        Path fileName = file.getFileName();
        return "Move " + (fileName != null ? fileName.toString() : file.toString()) + " to Trash";
    }

    @Override
    public void apply() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            moveToTrash(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to move " + file + " to Trash", e);
        }
        afterApply.run();
    }

    /**
     * Moves the given path to the OS trash. Overridable so tests don't touch the real Trash.
     */
    protected void moveToTrash(Path path) throws IOException {
        FileUtils.getInstance().moveToTrash(path.toFile());
    }
}
