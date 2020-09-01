package com.ogerardin.xpman.platform;

import com.ogerardin.xpman.util.exec.ProcessExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;

@Slf4j
public class WindowsPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(Path path) {
        String explorerParam = "/select,\"" + path.toString() + "\"";
        ProcessExecutor.exec("cmd", "/c", "explorer.exe " + explorerParam);
    }

    @Override
    public String revealLabel() {
        return "Show in Explorer";
    }

    @SneakyThrows
    @Override
    public void openUrl(URL url) {
        ProcessExecutor.exec("cmd", "/c", String.format("start %s", url.toString()));
    }

    @SneakyThrows
    @Override
    public void openFile(Path file) {
        ProcessExecutor.exec("cmd", "/c", String.format("start \"\" \"%s\"", file.toString()));
    }

    @SneakyThrows
    @Override
    public void startApp(Path app) {
        ProcessExecutor.exec("cmd", "/c", String.format("start /b \"X-Plane\" \"%s\"", app.toString()));
    }
}
