package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.util.platform.Platform;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A {@link Manifest} is the description of a Tool, with recipes to check if it is installed, install it from
 * the Internet, uninstall it, etc.
 */
@Data
@Slf4j
public class Manifest {

    @NonNull
    private final String name;
    private final String version;
    private final String description;
    /** target platform for this tool */
    @NonNull
    private final Platform platform;
    /** URL to download the tool from */
    @NonNull
    private final URL url;
    /** predicate to check if a given file matches this tool */
    @NonNull
    private final Predicate<Path> installChecker;

}
