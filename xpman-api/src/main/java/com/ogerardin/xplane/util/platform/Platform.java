package com.ogerardin.xplane.util.platform;

import lombok.NonNull;

import java.net.URL;
import java.nio.file.Path;

/**
 * Common interface for platform-specific operations
 */
public interface Platform {

    /** The int value of {@link com.sun.jna.Platform} that matches this platform */
    int getOsType();

    default String getCpuType() { return "unknown"; }

    default int getCpuCount() { return 1; }

    default boolean isCurrent() {
        return getOsType() == com.sun.jna.Platform.getOSType();
    }

    /** The platform-specific text corresponding to the action "reveal in Finder" or "show in Explorer" or equivalent */
    default String revealLabel() {
        return "Show in files";
    }

    /** Reveal in Finder / show in Explorer or equivalent */
    default void reveal(@NonNull Path path) {
        throw new UnsupportedOperationException();
    }

    default void openFile(@NonNull Path path) {
        throw new UnsupportedOperationException();
    }

    default void openUrl(@NonNull URL url) {
        throw new UnsupportedOperationException();
    }

    /**
     * Start an application from the specified path.
     * The nature of the path may vary depending on the platform (binary executable file, app bundle, etc.)
     */
    default void startApp(@NonNull Path app) {
        throw new UnsupportedOperationException();
    }

    /** Is the specified path an existing runnable for this platform? */
    default boolean isRunnable(@NonNull Path path)  {
        return false;
    }

    default String getVersion(Path app) {
        return null;
    }
}
