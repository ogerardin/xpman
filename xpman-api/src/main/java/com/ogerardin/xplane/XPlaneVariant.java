package com.ogerardin.xplane;

import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@RequiredArgsConstructor
public enum XPlaneVariant {
    MAC(Platforms.MAC, "X-Plane.app"),
    WINDOWS(Platforms.WINDOWS, "X-Plane.exe"),
    LINUX(Platforms.LINUX, "X-Plane-x86_64"),
    UNKNOWN(Platforms.UNKNOWN, "");

    private final Platform platform;
    private final String appFilename;

    public boolean applies(Path rootFolder) {
        if (appFilename == null) {
            return true;
        }
        return Files.exists(getAppPath(rootFolder));
    }

    public String getVersion(Path rootFolder) {
        return platform.getVersion(getAppPath(rootFolder));
    }

    public Path getAppPath(Path rootFolder) {
        return rootFolder.resolve(appFilename);
    }

}
