package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

/**
 * A generic container for files.
 */
public interface Archive {

    boolean isValidArchive();

    List<Path> getPaths();

    int entryCount();

    String getAsText(Path path) throws IOException;

    void extract(Path folder, ProgressListener progressListener) throws IOException;

    /**
     * Extracts entries from the archive that match the specified filter.
     *
     * @param folder           the target folder
     * @param filter           predicate to test each entry path; only matching entries are extracted
     * @param progressListener optional progress listener (may be null)
     */
    void extract(Path folder, Predicate<Path> filter, ProgressListener progressListener) throws IOException;

    /**
     * Extracts entries from the archive that are under the specified subpath.
     * The subpath prefix is stripped from extracted paths.
     *
     * @param folder           the target folder
     * @param subpath          the subpath within the archive to extract from
     * @param progressListener optional progress listener (may be null)
     */
    default void extract(Path folder, Path subpath, ProgressListener progressListener) throws IOException {
        throw new UnsupportedOperationException("Extract with subpath not implemented");
    }

    /**
     * Returns the path to the archive source file.
     *
     * @return the path to the archive file
     */
    Path getSourcePath();
}
