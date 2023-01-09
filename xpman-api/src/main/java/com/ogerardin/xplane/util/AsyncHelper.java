package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;

@UtilityClass
public class AsyncHelper {

    public void runAsync(Runnable task) {
        Executors.newSingleThreadExecutor().submit(task);

    }
}
