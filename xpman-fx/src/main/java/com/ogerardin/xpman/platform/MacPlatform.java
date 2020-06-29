package com.ogerardin.xpman.platform;

import com.ogerardin.xpman.exec.ProcessExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;

@Slf4j
public class MacPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(Path path) {
        ProcessExecutor.exec("open", "-R", path.toString());
    }

    @Override
    public String revealLabel() {
        return "Reveal in Finder";
    }

    @SneakyThrows
    @Override
    public void openInBrowser(URL url) {
        ProcessExecutor.exec("open", url.toString());
    }
}
