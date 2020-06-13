package com.ogerardin.xplane;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
public abstract class XPManTestBase {

    @SneakyThrows
    protected Path getXPRootFolder() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path xplaneRoot = Stream.of(
                userHome.resolve("Applications").resolve("X-Plane 11"),
                userHome.resolve("Desktop").resolve("X-Plane 11"),
                Paths.get(XPManTestBase.class.getResource("/X-Plane 11/").toURI())
        )
                .filter(path -> Files.isDirectory(path))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to find an X-Plane root folder"));

        log.info("\n\nUsing X-Plane root folder '{}'\n", xplaneRoot);
        return xplaneRoot;
    }
}
