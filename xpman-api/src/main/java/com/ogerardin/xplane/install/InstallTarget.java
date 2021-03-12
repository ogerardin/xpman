package com.ogerardin.xplane.install;

import java.io.IOException;

public interface InstallTarget {

    void install(InstallableArchive archive, Installer.ProgressListener progressListener) throws IOException;
}
