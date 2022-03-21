package com.ogerardin.xplane.util.platform;

import com.ogerardin.xplane.exec.CommandExecutor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.plist.XMLPropertyListConfiguration;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class MacPlatform implements Platform {

    @SneakyThrows
    @Override
    public void reveal(@NonNull Path path) {
        // if path is a directory, use any contained file otherwise the Finder will not select the directory
        if (Files.isDirectory(path) && ! path.getFileName().toString().endsWith(".app")) {
            try (Stream<Path> pathStream = Files.list(path)) {
                path = pathStream.findFirst().orElse(path);
            }
        }
        CommandExecutor.exec("open", "-R", path.toString());
    }

    @Override
    public String revealLabel() {
        return "Reveal in Finder";
    }

    @SneakyThrows
    @Override
    public void openUrl(@NonNull URL url) {
        CommandExecutor.exec("open", url.toString());
    }

    @Override
    @SneakyThrows
    public void startApp(@NonNull Path app) {
        CommandExecutor.exec("open", app.toString());
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return Files.isDirectory(path)
                && path.getFileName().toString().endsWith(".app");
    }

    @Override
    @SneakyThrows
    public void openFile(@NonNull Path file) {
        CommandExecutor.exec("open", file.toString());
    }

    @SneakyThrows
    public static String getMacVersion(Path appPath) {
        Path plistFile = appPath.resolve("Contents").resolve("info.plist");
        XMLPropertyListConfiguration plist = new XMLPropertyListConfiguration(plistFile.toFile());
        return plist.getString("CFBundleShortVersionString");
    }
}
