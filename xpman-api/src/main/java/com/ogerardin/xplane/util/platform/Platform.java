package com.ogerardin.xplane.util.platform;

import lombok.NonNull;

import java.net.URL;
import java.nio.file.Path;

public interface Platform {

    /** The int value of {@link com.sun.jna.Platform} that matches this platform */
    int getOsType();

    default boolean isCurrent() {
        return getOsType() == com.sun.jna.Platform.getOSType();
    }

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
