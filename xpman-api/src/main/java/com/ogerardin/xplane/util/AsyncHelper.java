package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@UtilityClass
public class AsyncHelper {

    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "xpman-async");
        t.setDaemon(true);
        return t;
    });

    public void runAsync(Runnable task) {
        // submit() would silently swallow any exception thrown by the task in the discarded Future
        EXECUTOR.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Async task failed: {}", task, e);
            }
        });
    }
}
