package com.ogerardin.xpman.platform;

import lombok.Getter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Platforms {

    @Getter(lazy = true)
    private final Platform current = currentPlatform();

    private Platform currentPlatform() {
        switch (com.sun.jna.Platform.getOSType()) {
            case com.sun.jna.Platform.MAC:
                return new MacPlatform();
            case com.sun.jna.Platform.WINDOWS:
                return new WindowsPlatform();
            case com.sun.jna.Platform.LINUX:
                return new LinuxPlatform();
            default:
                return new UnknownPlatform();
        }
    }
}
