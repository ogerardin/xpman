package com.ogerardin.xplane.install;

import com.ogerardin.xplane.util.ZipUtils;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Data
public class InstallableZip implements InstallableArchive {

    public final Path file;

    @Getter(lazy = true)
    private final List<Path> paths = loadPaths();

    @SneakyThrows
    private List<Path> loadPaths() {
        return ZipUtils.zipPaths(getFile()).collect(Collectors.toList());
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
    public void installTo(Path targetFolder, Installer.ProgressListener progressListener) throws IOException {
        log.info("Installing {} to {}", getFile(), targetFolder);
        int size = entryCount();
        AtomicInteger counter = new AtomicInteger();
        ZipUtils.unzip(this.file, targetFolder, filename -> {
            int count = counter.incrementAndGet();
//            log.debug("Progress: {} / {}= {}", count, size, (double) count / size);
            progressListener.installing((double) count / size, filename);
        });
    }

}
