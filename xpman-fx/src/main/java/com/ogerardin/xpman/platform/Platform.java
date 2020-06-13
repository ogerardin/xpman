package com.ogerardin.xpman.platform;

import java.io.IOException;
import java.nio.file.Path;

public interface Platform {

    /** Reveal in Finder / show in Explorer or equivalent */
    void reveal(Path path) throws IOException;
}
