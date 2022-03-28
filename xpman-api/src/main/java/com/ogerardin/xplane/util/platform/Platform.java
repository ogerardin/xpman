package com.ogerardin.xplane.util.platform;

import lombok.NonNull;

import java.net.URL;
import java.nio.file.Path;

public interface Platform {

    int getOsType();

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

    default boolean isRunnable(@NonNull Path path)  {
        return false;
    }
}
