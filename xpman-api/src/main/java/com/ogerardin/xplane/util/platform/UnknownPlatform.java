package com.ogerardin.xplane.util.platform;

import lombok.Getter;

public class UnknownPlatform implements Platform {

    @Getter
    public final int osType = com.sun.jna.Platform.UNSPECIFIED;

}
