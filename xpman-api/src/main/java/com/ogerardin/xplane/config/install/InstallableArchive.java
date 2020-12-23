package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.util.FileUtils;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
public class InstallableArchive {

    public final Path file;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        return FileUtils.zipPaths(getFile()).collect(Collectors.toList());
    }

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

    public void installTo(Path targetFolder) throws IOException {
        log.info("Installing {} to {}", getFile(), targetFolder);
        FileUtils.unzip(this.file, targetFolder);
    }

}
