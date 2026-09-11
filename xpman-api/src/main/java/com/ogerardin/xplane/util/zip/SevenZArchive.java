package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Data
public class SevenZArchive implements Archive {

    private static final int BUFFER_SIZE = 8192;
    private static final long PROGRESS_REPORT_INTERVAL = 64 * 1024;

    private final Path sevenZFile;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        try (SevenZFile szf = openSevenZ()) {
            return StreamSupport.stream(szf.getEntries().spliterator(), false)
                    .map(SevenZArchiveEntry::getName)
                    .map(Paths::get)
                    .toList();
        }
    }

    private SevenZFile openSevenZ() throws IOException {
        return SevenZFile.builder().setFile(sevenZFile.toFile()).get();
    }

    @Override
    public int entryCount() {
        return getPaths().size();
    }

    @Getter(lazy = true)
    private final boolean validArchive = computeValidArchive();

    private boolean computeValidArchive() {
        try {
            getPaths();
            return true;
        } catch (Exception e) {
            log.debug("Invalid archive: {}", getSevenZFile(), e);
            return false;
        }
    }

    @Override
    public String getAsText(Path path) throws IOException {
        try (SevenZFile szf = openSevenZ()) {
            for (SevenZArchiveEntry entry : szf.getEntries()) {
                if (!Paths.get(entry.getName()).equals(path)) {
                    continue;
                }
                try (InputStream inputStream = szf.getInputStream(entry);
                     Reader reader = new InputStreamReader(inputStream)) {
                    return IOUtils.toString(reader);
                }
            }
        }
        throw new FileNotFoundException(path.toString());
    }

    @Override
    public void extract(Path folder, ProgressListener progressListener) throws IOException {
        try (SevenZFile szf = openSevenZ()) {
            extractEntries(szf, folder, progressListener, null);
        }
    }

    @Override
    public void extract(Path folder, Path subpath, ProgressListener progressListener) throws IOException {
        try (SevenZFile szf = openSevenZ()) {
            extractEntries(szf, folder, progressListener, subpath);
        }
    }

    @Override
    public Path getSourcePath() {
        return sevenZFile;
    }

    private void extractEntries(SevenZFile szf, Path targetFolder, ProgressListener progressListener, Path subpath) throws IOException {
        Files.createDirectories(targetFolder);
        final Path normalizedTarget = targetFolder.toAbsolutePath().normalize();
        final List<? extends SevenZArchiveEntry> entries = StreamSupport.stream(szf.getEntries().spliterator(), false).toList();
        final long totalBytes = entries.stream().mapToLong(e -> Math.max(0, e.getSize())).sum();

        long copiedBytes = 0;
        long nextReport = 0;
        for (SevenZArchiveEntry entry : entries) {
            if (progressListener != null && copiedBytes >= nextReport) {
                progressListener.progress((double) copiedBytes / Math.max(1, totalBytes), "Extracting " + entry.getName());
                nextReport = copiedBytes + PROGRESS_REPORT_INTERVAL;
            }

            Path entryPath = Paths.get(entry.getName());

            if (subpath != null) {
                if (!entryPath.startsWith(subpath)) {
                    continue;
                }
                if (entryPath.getNameCount() <= subpath.getNameCount()) {
                    continue;
                }
                entryPath = entryPath.subpath(subpath.getNameCount(), entryPath.getNameCount());
            }

            final Path target = normalizedTarget.resolve(entryPath.toString()).normalize();
            if (!target.startsWith(normalizedTarget)) {
                throw new IOException("Blocked potentially malicious 7z entry: " + entry.getName());
            }
            if (entry.isDirectory()) {
                Files.createDirectories(target);
                continue;
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (InputStream is = szf.getInputStream(entry)) {
                try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(target))) {
                    final byte[] buffer = new byte[BUFFER_SIZE];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        os.write(buffer, 0, read);
                        copiedBytes += read;
                    }
                }
            }
        }
        if (progressListener != null) {
            progressListener.progress(1.00, "Done!");
        }
    }
}
