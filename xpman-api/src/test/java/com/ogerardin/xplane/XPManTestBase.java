package com.ogerardin.xplane;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.TestInstantiationException;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
public abstract class XPManTestBase {

    @SneakyThrows
    public static Path getXPRootFolder() {

        URL xp11rsc = XPManTestBase.class.getResource("/X-Plane 11/");
        if (xp11rsc != null) {
            return Paths.get(xp11rsc.toURI());
        }

        Path userHome = Paths.get(System.getProperty("user.home"));
        Path xplaneRoot = Stream.of(
//                userHome.resolve("Applications").resolve("X-Plane 11"),
                userHome.resolve("Desktop").resolve("X-Plane 11")
        )
                .filter(path -> Files.isDirectory(path))
                .findFirst()
                .orElseThrow(() -> new TestInstantiationException("Failed to find an X-Plane root folder"));

        log.info("\n\nUsing X-Plane root folder '{}'\n", xplaneRoot);
        return xplaneRoot;
    }
}
