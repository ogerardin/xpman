package com.ogerardin.xplane.install;

import java.io.IOException;

/**
 * Represents a potential target for a {@link InstallableArchive}.
 */
public interface InstallTarget {

    void install(InstallableArchive archive, Installer.ProgressListener progressListener) throws IOException;
}
