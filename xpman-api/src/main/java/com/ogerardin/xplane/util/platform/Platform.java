package com.ogerardin.xplane.util.platform;

import java.net.URL;
import java.nio.file.Path;

public interface Platform {

    default String revealLabel() {
        return "Show in files";
    }

    /** Reveal in Finder / show in Explorer or equivalent */
    default void reveal(Path path) {
        throw new UnsupportedOperationException();
    }

    default void openFile(Path path) {
        throw new UnsupportedOperationException();
    }

    default void openUrl(URL url) {
        throw new UnsupportedOperationException();
    }

    default void startApp(Path app) {
        throw new UnsupportedOperationException();
    }
}
