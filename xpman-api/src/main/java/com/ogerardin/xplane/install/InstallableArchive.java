package com.ogerardin.xplane.install;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface InstallableArchive {

    boolean isValidArchive();

    List<Path> getPaths();

    int entryCount();

    void installTo(Path targetFolder, ProgressListener progressListener) throws IOException;

    String getAsText(Path path) throws IOException;

}
