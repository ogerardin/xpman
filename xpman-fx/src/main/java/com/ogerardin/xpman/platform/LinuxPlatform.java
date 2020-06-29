package com.ogerardin.xpman.platform;

import com.ogerardin.xpman.exec.ProcessExecutor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;

@Slf4j
public class LinuxPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(Path path) {
        // https://askubuntu.com/a/1109917/325617
        String shellParam = String.format("gtk-launch \"$(xdg-mime query default inode/directory)\" '%s'", path.toString());
        ProcessExecutor.exec("sh", "-c", shellParam);
    }

    @Override
    public String revealLabel() {
        return "Show in Files";
    }

    @SneakyThrows
    @Override
    public void openInBrowser(URL url) {
        ProcessExecutor.exec("xdg-open", url.toString());
    }
}
