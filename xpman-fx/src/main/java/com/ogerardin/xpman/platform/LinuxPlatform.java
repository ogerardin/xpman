package com.ogerardin.xpman.platform;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class LinuxPlatform implements Platform {

    @SneakyThrows
    @Override
    @UserLabel("Show in Files")
    public void reveal(Path path) {
        // https://askubuntu.com/a/1109917/325617
        String shellParam = String.format("gtk-launch \"$(xdg-mime query default inode/directory)\" '%s'", path.toString());
        Process p = new ProcessBuilder("sh", "-c", shellParam).start();
        log.debug("Started process {}", p);
        int exitValue = p.waitFor();
        log.debug("Process exited with value {}", exitValue);
    }
}
