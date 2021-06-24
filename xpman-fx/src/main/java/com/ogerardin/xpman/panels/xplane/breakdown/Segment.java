package com.ogerardin.xpman.panels.xplane.breakdown;

import javafx.application.Platform;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.SegmentedBar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Slf4j
public class Segment extends SegmentedBar.Segment {
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private final SegmentType type;
    private final Path path;

    public Segment(SegmentType type, Path path) {
        super(1.0);
        this.type = type;
        this.path = path;
        setText(type.getText());

        executor.submit(new DirectorySizeComputer());
    }

    private final class DirectorySizeComputer implements Runnable{
        @SneakyThrows
        @Override
        public void run() {
            final Path path = getPath();
            log.debug("Computing size of {}", path);
            long size = Files.walk(path)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();
            log.debug("Size of {}: {}", path, size);
            Platform.runLater(() -> setValue(size));
        }
    }
}
