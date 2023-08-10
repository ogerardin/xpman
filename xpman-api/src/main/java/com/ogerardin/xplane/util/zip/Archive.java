package com.ogerardin.xplane.util.zip;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Archive {

    boolean isValidArchive();

    List<Path> getPaths();

    int entryCount();

    String getAsText(Path path) throws IOException;

    void extract(Path folder, ProgressListener progressListener) throws IOException;
}
