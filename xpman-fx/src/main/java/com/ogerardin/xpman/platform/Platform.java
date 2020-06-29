package com.ogerardin.xpman.platform;

import java.net.URL;
import java.nio.file.Path;

public interface Platform {

    /** Reveal in Finder / show in Explorer or equivalent */
    default void reveal(Path path) {}

    default String revealLabel() {
        return "Show in files";
    }

    default void openInBrowser(URL url) {}
}
