package com.ogerardin.xplane.install;

import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.DryRunArchive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Represents a potential target for a {@link Archive}.
 */
public interface InstallTarget {

    /**
     * Extracts the archive where it belongs. Implementations must only write through the
     * {@code extract} methods of the given archive (so that {@link #overwrittenFiles} can run them
     * against a {@link DryRunArchive}) and must have no other side effect.
     */
    void unpack(Archive archive, ProgressListener progressListener) throws IOException;

    default void unpack(Archive archive) throws IOException {
        unpack(archive, (_, _) -> {});
    }

    /** Refreshes this target's content after an install. */
    void reload();

    default void install(Archive archive, ProgressListener progressListener) throws IOException {
        unpack(archive, progressListener);
        reload();
    }

    /**
     * Dry run of {@link #install}: returns the existing files that installing the archive would
     * overwrite. Nothing is written and nothing is reloaded.
     */
    default List<Path> overwrittenFiles(Archive archive) throws IOException {
        DryRunArchive dryRun = new DryRunArchive(archive);
        unpack(dryRun);
        return dryRun.overwrites();
    }

}
