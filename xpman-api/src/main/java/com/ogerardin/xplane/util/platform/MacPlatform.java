package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.exec.ProcessExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class MacPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(Path path) {
        // if path is a directory, use any contained file otherwise the Finder will not select the directory
        if (Files.isDirectory(path) && ! path.getFileName().toString().endsWith(".app")) {
            try (Stream<Path> pathStream = Files.list(path)) {
                path = pathStream.findAny().orElse(path);
            }
        }
        ProcessExecutor.exec("open", "-R", path.toString());
    }

    @Override
    public String revealLabel() {
        return "Reveal in Finder";
    }

    @SneakyThrows
    @Override
    public void openUrl(URL url) {
        ProcessExecutor.exec("open", url.toString());
    }

    @Override
    @SneakyThrows
    public void startApp(Path app) {
        ProcessExecutor.exec("open", app.toString());
    }

    @Override
    @SneakyThrows
    public void openFile(Path file) {
        ProcessExecutor.exec("open", file.toString());
    }
}
