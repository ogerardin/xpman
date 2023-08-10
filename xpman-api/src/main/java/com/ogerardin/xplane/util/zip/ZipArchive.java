package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Data
public class ZipArchive implements Archive {
    //TODO merge ZipUtils into this class

    public final Path zipFile;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        return ZipUtils.zipPaths(this.zipFile).toList();
    }

    @Override
    public int entryCount() {
        return getPaths().size();
    }

    @Override
    public boolean isValidArchive() {
        try {
            //noinspection ResultOfMethodCallIgnored
            getPaths();
            return true;
        } catch (Exception e) {
            log.error("Invalid archive", e);
            return false;
        }
    }

    @Override
    public String getAsText(Path path) throws IOException {
        return ZipUtils.getAsText(this.zipFile, path);
    }

    @Override
    public void extract(Path folder, ProgressListener progressListener) throws IOException {
        ZipUtils.unzip(this.zipFile, folder, progressListener);
    }
}
