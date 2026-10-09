package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An {@link Archive} decorator that writes nothing: its {@code extract} methods only record which
 * existing regular files the extraction would overwrite. Passing it to code that extracts an archive
 * turns that code into a dry run.
 */
@RequiredArgsConstructor
public class DryRunArchive implements Archive {

    /** The extraction methods, which this class implements instead of delegating. */
    private interface Extraction {
        void extract(Path folder, ProgressListener progressListener);
        void extract(Path folder, Predicate<Path> filter, ProgressListener progressListener);
        void extract(Path folder, Path subpath, ProgressListener progressListener);
    }

    @Delegate(excludes = Extraction.class)
    private final Archive archive;

    private final List<Path> overwrites = new ArrayList<>();

    /** The existing files that the extractions performed so far would have overwritten. */
    public List<Path> overwrites() {
        return List.copyOf(overwrites);
    }

    @Override
    public void extract(Path folder, ProgressListener progressListener) {
        extract(folder, path -> true, progressListener);
    }

    @Override
    public void extract(Path folder, Predicate<Path> filter, ProgressListener progressListener) {
        recordOverwrites(folder, entry -> ArchiveExtractor.resolveEntryPath(entry, null, filter));
    }

    // mirrors the subpath handling of ZipArchive/SevenZArchive.extractEntries
    @Override
    public void extract(Path folder, Path subpath, ProgressListener progressListener) {
        recordOverwrites(folder, entry -> ArchiveExtractor.resolveEntryPath(entry, subpath, path -> true));
    }

    /** Records the targets of the entries selected by {@code relativeTarget} that already exist as files. */
    private void recordOverwrites(Path folder, Function<Path, Optional<Path>> relativeTarget) {
        archive.getPaths().stream()
                .map(relativeTarget)
                .flatMap(Optional::stream)
                .map(folder::resolve)
                .filter(Files::isRegularFile)
                .forEach(overwrites::add);
    }
}
