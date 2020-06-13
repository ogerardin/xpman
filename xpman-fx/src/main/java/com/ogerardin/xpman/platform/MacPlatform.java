package com.ogerardin.xpman.platform;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class MacPlatform implements Platform {

    @SneakyThrows
    @Override
    @UserLabel("Reveal in Finder")
    public void reveal(Path path) {
        Process p = new ProcessBuilder("open", "-R", path.toString()).start();
        log.debug("Started process {}", p);
        int exitValue = p.waitFor();
        log.debug("Process exited with value {}", exitValue);
    }
}
