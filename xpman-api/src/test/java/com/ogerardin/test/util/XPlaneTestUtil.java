package com.ogerardin.test.util;

import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Locates a real X-Plane installation for tests, if one can be found.
 * The {@code XPMAN_XPLANE_ROOT} environment variable can be used to override auto-detection.
 */
@Slf4j
public class XPlaneTestUtil {

    @Getter(lazy = true)
    private static final Path defaultXPRootFolder = computeDefaultXPRootFolder();

    @SneakyThrows
    private static Path computeDefaultXPRootFolder() {
        Path override = getXpRootOverride();
        if (override != null) {
            log.info("\n\nUsing X-Plane root folder '{}' (from XPMAN_XPLANE_ROOT)\n", override);
            return override;
        }
        Path xplaneRoot = getCandidateRootFolders().stream()
                .filter(Files::isDirectory)
                .findFirst()
                .orElseThrow(() -> new InvalidConfig(
                        "Failed to find an X-Plane root folder; set the XPMAN_XPLANE_ROOT environment variable to point to your X-Plane installation"));

        log.info("\n\nUsing X-Plane root folder '{}'\n", xplaneRoot);
        return xplaneRoot;
    }

    @SneakyThrows
    private static Path getXpRootOverride() {
        String override = System.getenv("XPMAN_XPLANE_ROOT");
        if (override == null || override.isBlank()) {
            return null;
        }
        Path path = Paths.get(override);
        if (!Files.isDirectory(path)) {
            throw new InvalidConfig("XPMAN_XPLANE_ROOT is set to '" + override + "' but this is not an existing folder");
        }
        return path;
    }

    private static List<Path> getCandidateRootFolders() {
        Path userHome = Paths.get(System.getProperty("user.home"));
        return Platforms.getCurrent().getCandidateInstallBaseFolders(userHome).stream()
                .flatMap(base -> Stream.of("X-Plane 12", "X-Plane 11").map(base::resolve))
                .toList();
    }
}
