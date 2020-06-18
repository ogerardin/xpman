package com.ogerardin.xpman.platform;

import com.ogerardin.xpman.exec.ProcessExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;

@Slf4j
public class WindowsPlatform implements Platform {

    @SneakyThrows
    @Override
    @UserLabel("Show in Explorer")
    public void reveal(Path path) {
        String explorerParam = "/select,\"" + path.toString() + "\"";
        ProcessExecutor.exec("explorer.exe", explorerParam);
    }

    @SneakyThrows
    @Override
    public void openInBrowser(URL url) {
        ProcessExecutor.exec("cmd", "/c", "start", url.toString());
    }
}
