package com.ogerardin.xplane.exec;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A daemon Thread that reads the specified {@link InputStream} as long as EOF is not reached and stores
 * the read lines into a List. The Thread is started as soon as the constructor is called.
 */
@EqualsAndHashCode(callSuper = true)
@Data
class StreamGobbler extends Thread {
    private final InputStream inputStream;
    private IOException exception = null;
    private List<String> lines = new ArrayList<>();

    StreamGobbler(InputStream inputStream) {
        this.inputStream = inputStream;
        this.setDaemon(true);
        this.start();
    }

    List<String> getLines() {
        return lines;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        catch (IOException ioe) {
            exception = ioe;
        }
    }
}
