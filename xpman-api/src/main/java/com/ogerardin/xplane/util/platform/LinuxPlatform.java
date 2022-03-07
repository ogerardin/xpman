package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.exec.CommandExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class LinuxPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(Path path) {
        // https://askubuntu.com/a/1109917/325617
        String shellParam = String.format("gtk-launch \"$(xdg-mime query default inode/directory)\" '%s'", path.toString());
        CommandExecutor.exec("sh", "-c", shellParam);
    }

    @Override
    public String revealLabel() {
        return "Show in Files";
    }

    @SneakyThrows
    @Override
    public void openUrl(URL url) {
        CommandExecutor.exec("xdg-open", url.toString());
    }

    @SneakyThrows
    @Override
    public void openFile(Path file) {
        CommandExecutor.exec("xdg-open", file.toString());
    }

    @SneakyThrows
    @Override
    public void startApp(Path app) {
        CommandExecutor.exec("sh", "-c", app.toString());
    }

    @Override
    public boolean isRunnable(Path path) {
        return Files.isExecutable(path);
    }
}
