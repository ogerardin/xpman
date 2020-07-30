package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.Inspection;

import java.nio.file.Path;

public interface Installer extends Inspection<Path> {

    void install(Path source);
}
