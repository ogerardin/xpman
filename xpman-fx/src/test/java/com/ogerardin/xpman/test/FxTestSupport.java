package com.ogerardin.xpman.test;

import javafx.application.Platform;
import org.junit.jupiter.api.Assumptions;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/** Runs desktop UI assertions on a single JavaFX toolkit without launching real updates. */
public final class FxTestSupport {
    private static boolean started;

    public static synchronized void start() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Desktop JavaFX tests require a display");
        if (started) return;
        CountDownLatch ready = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            ready.countDown();
        });
        if (!ready.await(10, TimeUnit.SECONDS)) throw new AssertionError("JavaFX startup timed out");
        started = true;
    }

    public static <T> T onFx(Callable<T> work) throws Exception {
        FutureTask<T> task = new FutureTask<>(work);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
