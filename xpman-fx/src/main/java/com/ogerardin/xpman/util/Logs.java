package com.ogerardin.xpman.util;

import com.ogerardin.xplane.util.platform.Platforms;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** Provides the XPman log directory and log archive export. */
@UtilityClass
public class Logs {

    public Path logDir() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path logDir = Platforms.getCurrent().getLogDir(userHome);
        try {
            return Files.createDirectories(logDir);
        } catch (IOException e) {
            Path fallback = userHome.resolve(".xpman/logs");
            try {
                return Files.createDirectories(fallback);
            } catch (IOException ignored) {
                return fallback;
            }
        }
    }

    public void zip(Path sourceDir, Path destination) throws IOException {
        Path normalizedDestination = destination.toAbsolutePath().normalize();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(destination));
             var files = Files.list(sourceDir)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(file -> !file.toAbsolutePath().normalize().equals(normalizedDestination))
                    .toList()) {
                zip.putNextEntry(new ZipEntry(file.getFileName().toString()));
                Files.copy(file, zip);
                zip.closeEntry();
            }
        }
    }
}
