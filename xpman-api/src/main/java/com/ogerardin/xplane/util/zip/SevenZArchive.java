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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

@Slf4j
@Data
public class SevenZArchive implements Archive {

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
            //noinspection ResultOfMethodCallIgnored
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
        extract(folder, path -> true, progressListener);
    }

    @Override
    public void extract(Path folder, Predicate<Path> filter, ProgressListener progressListener) throws IOException {
        try (SevenZFile szf = openSevenZ()) {
            extractEntries(szf, folder, progressListener, null, filter);
        }
    }

    @Override
    public void extract(Path folder, Path subpath, ProgressListener progressListener) throws IOException {
        try (SevenZFile szf = openSevenZ()) {
            extractEntries(szf, folder, progressListener, subpath, path -> true);
        }
    }

    @Override
    public Path getSourcePath() {
        return sevenZFile;
    }

    private void extractEntries(SevenZFile szf, Path targetFolder, ProgressListener progressListener, Path subpath, Predicate<Path> filter) throws IOException {
        List<ArchiveExtractor.Entry> entries = StreamSupport.stream(szf.getEntries().spliterator(), false)
                .map(entry -> new ArchiveExtractor.Entry(entry.getName(), entry.getSize(), entry.isDirectory(),
                        () -> szf.getInputStream(entry)))
                .toList();
        ArchiveExtractor.extractEntries(entries, targetFolder, progressListener, subpath, filter, "7z");
    }
}
