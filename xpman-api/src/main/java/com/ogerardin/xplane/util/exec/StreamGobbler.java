package com.ogerardin.xplane.util.exec;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * A daemon Thread that reads the specified {@link InputStream} as long as EOF is not reached and stores
 * the read lines into a List. The Thread is started as soon as the constructor is called.
 */
@EqualsAndHashCode(callSuper = true)
@Data
class StreamGobbler extends Thread {
    private final InputStream inputStream;
    private final Consumer<String> lineConsumer;

    private IOException exception = null;

    StreamGobbler(InputStream inputStream, Consumer<String> lineConsumer) {
        this.inputStream = inputStream;
        this.lineConsumer = lineConsumer;
        this.setDaemon(true);
        this.start();
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                lineConsumer.accept(line);
            }
        }
        catch (IOException ioe) {
            exception = ioe;
        }
    }
}
