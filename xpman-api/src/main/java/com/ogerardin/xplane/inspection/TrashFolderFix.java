package com.ogerardin.xplane.inspection;

import com.sun.jna.platform.FileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Fix} that moves a folder to the OS trash.
 *
 * <p>Used by inspections that want to offer removal of problematic content
 * (e.g. a livery that requires a missing engine mod). The folder is moved to the OS trash
 * when the fix is applied, allowing the user to restore it if needed.</p>
 */
public class TrashFolderFix implements Fix {

    private final Path folder;

    public TrashFolderFix(Path folder) {
        this.folder = folder;
    }

    @Override
    public String label() {
        return "remove livery";
    }

    @Override
    public void apply() {
        if (!Files.exists(folder)) {
            return;
        }
        try {
            moveToTrash(folder);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to move " + folder + " to Trash", e);
        }
    }

    /**
     * Moves the given folder to the OS trash. Overridable so tests don't touch the real Trash.
     */
    protected void moveToTrash(Path folder) throws IOException {
        FileUtils.getInstance().moveToTrash(folder.toFile());
    }
}
