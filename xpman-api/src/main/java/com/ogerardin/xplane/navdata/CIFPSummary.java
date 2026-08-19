package com.ogerardin.xplane.navdata;

import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * A summary node representing all CIFP per-airport files in a directory.
 * Displays as a single "CIFP (N files)" entry rather than enumerating each file.
 */
@Getter
@ToString
public class CIFPSummary implements NavDataItem {

    private final Path cifpDir;

    private final List<Path> files;

    public CIFPSummary(Path cifpDir) {
        this.cifpDir = cifpDir;
        this.files = scanCifpFiles(cifpDir);
    }

    private static List<Path> scanCifpFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            return Collections.emptyList();
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.toString().endsWith(".dat"))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String getName() {
        return "CIFP (" + files.size() + " files)";
    }

    @Override
    public Boolean getExists() {
        return Files.isDirectory(cifpDir) && !files.isEmpty();
    }
}
