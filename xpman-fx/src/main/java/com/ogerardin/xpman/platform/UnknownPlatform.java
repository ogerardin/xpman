package com.ogerardin.xpman.platform;

import java.nio.file.Path;

public class UnknownPlatform implements Platform {

    @Override
    public void reveal(Path path) {
        //nop
    }
}
