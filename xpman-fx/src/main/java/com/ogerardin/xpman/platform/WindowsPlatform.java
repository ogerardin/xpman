package com.ogerardin.xpman.platform;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class WindowsPlatform implements Platform {

    @SneakyThrows
    @Override
    @UserLabel("Show in Explorer")
    public void reveal(Path path) {
        String explorerParam = "/select,\"" + path.toString() + "\"";
        Process p = new ProcessBuilder("explorer.exe", explorerParam).start();
        log.debug("Started process {}", p);
        int exitValue = p.waitFor();
        log.debug("Process exited with value {}", exitValue);
    }
}
