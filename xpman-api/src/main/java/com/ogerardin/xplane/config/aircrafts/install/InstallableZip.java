package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.util.FileUtils;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Data
public class InstallableZip {

    public final Path file;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        return FileUtils.zipPaths(getFile()).collect(Collectors.toList());
    }

    public boolean isValidZip() {
        try {
            //noinspection ResultOfMethodCallIgnored
            getPaths();
            return true;
        } catch (Exception e) {
            log.error("Invalid zip", e);
            return false;
        }
    }

}
