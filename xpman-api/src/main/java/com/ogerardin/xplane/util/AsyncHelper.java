package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@UtilityClass
public class AsyncHelper {

    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "xpman-async");
        t.setDaemon(true);
        return t;
    });

    public void runAsync(Runnable task) {
        EXECUTOR.submit(task);
    }
}
