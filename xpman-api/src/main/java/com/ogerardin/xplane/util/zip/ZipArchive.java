package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Data
public class ZipArchive implements Archive {

    private static final int BUFFER_SIZE = 8192;
    private static final long PROGRESS_REPORT_INTERVAL = 64 * 1024;

    public final Path zipFile;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        try (ZipFile zip = openZip()) {
            // materialize before closing the ZipFile: entries are backed by it
            return zip.stream()
                    .map(ZipEntry::getName)
                    .map(Paths::get)
                    .toList();
        }
    }

    private ZipFile openZip() throws IOException {
        return new ZipFile(zipFile.toFile());
    }

    @Override
    public int entryCount() {
        return getPaths().size();
    }

    @Getter(lazy = true)
    private final boolean validArchive = computeValidArchive();

    private boolean computeValidArchive() {
        try {
            // materialize entry list: forces parsing of the central directory
            getPaths();
            return true;
        } catch (Exception e) {
            log.error("Invalid archive: {}", getZipFile(), e);
            return false;
        }
    }

    @Override
    public String getAsText(Path path) throws IOException {
        try (ZipFile zip = openZip()) {
            for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                if (!Paths.get(entry.getName()).equals(path)) {
                    continue;
                }
                InputStream inputStream = zip.getInputStream(entry);
                try (Reader reader = new InputStreamReader(inputStream)) {
                    return IOUtils.toString(reader);
                }
            }
        }
        throw new FileNotFoundException(path.toString());
    }

    @Override
    public void extract(Path folder, ProgressListener progressListener) throws IOException {
        try (ZipFile zip = openZip()) {
            extractEntries(zip, folder, progressListener);
        }
    }

    private void extractEntries(ZipFile zip, Path targetFolder, ProgressListener progressListener) throws IOException {
        Files.createDirectories(targetFolder);
        final Path normalizedTarget = targetFolder.toAbsolutePath().normalize();
        final List<? extends ZipEntry> entries = zip.stream().toList();
        final long totalBytes = entries.stream().mapToLong(e -> Math.max(0, e.getSize())).sum();

        long copiedBytes = 0;
        long nextReport = 0;
        for (ZipEntry entry : entries) {
            if (progressListener != null && copiedBytes >= nextReport) {
                progressListener.progress((double) copiedBytes / Math.max(1, totalBytes), "Extracting " + entry.getName());
                nextReport = copiedBytes + PROGRESS_REPORT_INTERVAL;
            }
            final Path target = normalizedTarget.resolve(entry.getName()).normalize();
            // protect against "zip slip" (entries with path traversal outside the target folder)
            if (!target.startsWith(normalizedTarget)) {
                throw new IOException("Blocked potentially malicious zip entry: " + entry.getName());
            }
            if (entry.isDirectory()) {
                Files.createDirectories(target);
                continue;
            }
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            try (InputStream is = zip.getInputStream(entry)) {
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
