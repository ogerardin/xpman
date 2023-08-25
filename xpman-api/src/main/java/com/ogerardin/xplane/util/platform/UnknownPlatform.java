package com.ogerardin.xplane.util.platform;

import lombok.Getter;
import lombok.NonNull;

import java.net.URL;
import java.nio.file.Path;

public class UnknownPlatform implements Platform {

    @Getter
    public final int osType = com.sun.jna.Platform.UNSPECIFIED;

    @Override
    public void reveal(@NonNull Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openFile(@NonNull Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void openUrl(@NonNull URL url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startApp(@NonNull Path app) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRunnable(@NonNull Path path) {
        return false;
    }

    @Override
    public String getVersion(Path app) {
        return null;
    }
}
