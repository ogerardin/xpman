package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.util.platform.Platform;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A {@link Manifest} is the description of a Tool, with recipes to check if it is installed, install it from the
 * Internet, uninstall it, etc.
 *
 * @param platform target platform for this tool
 * @param url URL to download the tool from
 * @param installChecker predicate to check if a given file matches this tool
 */
@Slf4j
public record Manifest(
        @NonNull String name,
        URL homepage,
        String version,
        String description,
        @NonNull Platform platform,
        XPlaneMajorVersion xplaneVersion,
        @NonNull URL url,
        @NonNull Predicate<Path> installChecker) {
}
