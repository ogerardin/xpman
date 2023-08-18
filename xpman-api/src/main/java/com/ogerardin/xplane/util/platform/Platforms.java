package com.ogerardin.xplane.util.platform;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.Arrays;

/**
 * Known platforms as an enum.
 * The actual implementation is delegated to keep this enum short and readable.
 */
@RequiredArgsConstructor
public enum Platforms implements Platform {
    WINDOWS(new WindowsPlatform()),
    MAC(new MacPlatform()),
    LINUX(new LinuxPlatform()),
    UNKNOWN(new UnknownPlatform());

    @Delegate
    private final Platform delegate;

    @Getter(lazy = true)
    private static final Platform current = currentPlatform();

    private static Platform currentPlatform() {
        int osType = com.sun.jna.Platform.getOSType();
        return getPlatform(osType);
    }

    static Platform getPlatform(int osType) {
        return Arrays.stream(values())
                .filter(p -> p.getOsType() == osType)
                .findAny()
                .orElse(UNKNOWN);
    }

}
