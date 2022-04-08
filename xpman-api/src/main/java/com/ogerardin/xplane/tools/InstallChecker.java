package com.ogerardin.xplane.tools;

import java.nio.file.Path;

@FunctionalInterface
public interface InstallChecker {
    boolean isInstalled(Path path);
}
