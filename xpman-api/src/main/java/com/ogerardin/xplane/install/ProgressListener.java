package com.ogerardin.xplane.install;

public interface ProgressListener {
    void progress(double percent, String message);

    default void output(String message) {}
}
