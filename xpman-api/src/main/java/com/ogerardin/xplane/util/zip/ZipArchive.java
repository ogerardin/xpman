package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
@Data
public class ZipArchive implements Archive {

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
            //noinspection ResultOfMethodCallIgnored
            getPaths();
            return true;
        } catch (Exception e) {
            log.debug("Invalid archive: {}", getZipFile(), e);
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
        extract(folder, path -> true, progressListener);
    }

    @Override
    public void extract(Path folder, Predicate<Path> filter, ProgressListener progressListener) throws IOException {
        try (ZipFile zip = openZip()) {
            extractEntries(zip, folder, progressListener, null, filter);
        }
    }

    @Override
    public void extract(Path folder, Path subpath, ProgressListener progressListener) throws IOException {
        try (ZipFile zip = openZip()) {
            extractEntries(zip, folder, progressListener, subpath, path -> true);
        }
    }

    @Override
    public Path getSourcePath() {
        return zipFile;
    }

    private void extractEntries(ZipFile zip, Path targetFolder, ProgressListener progressListener, Path subpath, Predicate<Path> filter) throws IOException {
        List<ArchiveExtractor.Entry> entries = zip.stream()
                .map(entry -> new ArchiveExtractor.Entry(entry.getName(), entry.getSize(), entry.isDirectory(),
                        () -> zip.getInputStream(entry)))
                .toList();
        ArchiveExtractor.extractEntries(entries, targetFolder, progressListener, subpath, filter, "zip");
    }

}
