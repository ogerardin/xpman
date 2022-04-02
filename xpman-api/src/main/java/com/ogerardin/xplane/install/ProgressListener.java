package com.ogerardin.xplane.install;

public interface ProgressListener {
    void progress(Double percent, String message);

    default void output(String message) {}
}
