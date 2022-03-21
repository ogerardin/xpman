package com.ogerardin.xplane.util.platform;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Platforms {

    @Getter(lazy = true)
    private final Platform current = currentPlatform();

    private Platform currentPlatform() {
        int osType = com.sun.jna.Platform.getOSType();
        return getPlatform(osType);
    }

    public Platform getPlatform(int osType) {
        return switch (osType) {
            case com.sun.jna.Platform.MAC -> new MacPlatform();
            case com.sun.jna.Platform.WINDOWS -> new WindowsPlatform();
            case com.sun.jna.Platform.LINUX -> new LinuxPlatform();
            default -> new UnknownPlatform();
        };
    }
}
