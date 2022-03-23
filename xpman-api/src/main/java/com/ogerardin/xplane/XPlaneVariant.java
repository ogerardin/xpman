package com.ogerardin.xplane;

import com.ogerardin.xplane.util.platform.LinuxPlatform;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.ogerardin.xplane.util.platform.WindowsPlatform;
import com.sun.jna.Platform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum XPlaneVariant {
    MAC(Platform.MAC, "X-Plane.app", MacPlatform::getMacAppVersion),
    WINDOWS(Platform.WINDOWS, "X-Plane.exe", WindowsPlatform::getPEVersion),
    LINUX(Platform.LINUX, "X-Plane-x86_64", LinuxPlatform::getELFVersion),
    UNKNOWN(Platform.UNSPECIFIED, "", path -> "unknown");

    private final int osType;
    private final String appFilename;
    private final Function<Path, String> versionFetcher;

    public boolean applies(Path rootFolder) {
        if (appFilename == null) {
            return true;
        }
        return Files.exists(getAppPath(rootFolder));
    }

    public String getVersion(Path rootFolder) {
        return versionFetcher.apply(getAppPath(rootFolder));
    }

    public Path getAppPath(Path rootFolder) {
        return rootFolder.resolve(appFilename);
    }


}
