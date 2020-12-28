package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.Inspection;

import java.nio.file.Path;

public interface Installer extends Inspection<Path> {

    default void install(Path source) {
        install(source, null);
    }

    void install(Path source, ProgressListener progressListener);

    interface ProgressListener {
        void installing(double percent, String message);
    }
}
