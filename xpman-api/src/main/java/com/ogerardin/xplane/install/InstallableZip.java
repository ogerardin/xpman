package com.ogerardin.xplane.install;

import com.ogerardin.xplane.util.ZipUtils;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Data
public class InstallableZip implements InstallableArchive {

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
    public void installTo(Path targetFolder, ProgressListener progressListener) throws IOException {
        log.info("Installing {} to {}", getZipFile(), targetFolder);
        ZipUtils.unzip(this.zipFile, targetFolder, progressListener);
    }

    @Override
    public String getAsText(Path path) throws IOException {
        return ZipUtils.getAsText(this.zipFile, path);
    }
}
