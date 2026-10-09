package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Shared extraction logic for archive formats with different entry APIs. */
final class ArchiveExtractor {

    private static final long PROGRESS_REPORT_INTERVAL = 64L * 1024;

    private ArchiveExtractor() {}

    static Optional<Path> resolveEntryPath(Path entryPath, Path subpath, Predicate<Path> filter) {
        if (subpath != null) {
            if (!entryPath.startsWith(subpath) || entryPath.getNameCount() <= subpath.getNameCount()) {
                return Optional.empty();
            }
            entryPath = entryPath.subpath(subpath.getNameCount(), entryPath.getNameCount());
        }
        return filter.test(entryPath) ? Optional.of(entryPath) : Optional.empty();
    }

    static void extractEntries(List<Entry> entries, Path targetFolder, ProgressListener progressListener,
                               Path subpath, Predicate<Path> filter, String archiveType) throws IOException {
        Files.createDirectories(targetFolder);
        Path normalizedTarget = targetFolder.toAbsolutePath().normalize();
        long totalBytes = entries.stream().mapToLong(entry -> Math.max(0, entry.size())).sum();
        long copiedBytes = 0;
        long nextReport = 0;

        for (Entry entry : entries) {
            if (progressListener != null && copiedBytes >= nextReport) {
                progressListener.progress((double) copiedBytes / Math.max(1, totalBytes), "Extracting " + entry.name());
                nextReport = copiedBytes + PROGRESS_REPORT_INTERVAL;
            }
            Optional<Path> entryPath = resolveEntryPath(Paths.get(entry.name()), subpath, filter);
            if (entryPath.isPresent()) {
                copiedBytes += copyEntry(entry, entryPath.get(), normalizedTarget, archiveType);
            }
        }
        if (progressListener != null) {
            progressListener.progress(1.00, "Done!");
        }
    }

    private static long copyEntry(Entry entry, Path entryPath, Path normalizedTarget, String archiveType)
            throws IOException {
        Path target = normalizedTarget.resolve(entryPath.toString()).normalize();
        if (!target.startsWith(normalizedTarget)) {
            throw new IOException("Blocked potentially malicious " + archiveType + " entry: " + entry.name());
        }
        if (entry.directory()) {
            Files.createDirectories(target);
            return 0;
        }
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        try (InputStream input = entry.inputStream().open();
             var output = new BufferedOutputStream(Files.newOutputStream(target))) {
            return input.transferTo(output);
        }
    }

    record Entry(String name, long size, boolean directory, InputStreamSupplier inputStream) {}

    @FunctionalInterface
    interface InputStreamSupplier {
        InputStream open() throws IOException;
    }
}
