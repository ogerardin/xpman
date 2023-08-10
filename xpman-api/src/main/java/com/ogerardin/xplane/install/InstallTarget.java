package com.ogerardin.xplane.install;

import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;

/**
 * Represents a potential target for a {@link Archive}.
 */
public interface InstallTarget {

    void install(Archive archive, ProgressListener progressListener) throws IOException;

}
