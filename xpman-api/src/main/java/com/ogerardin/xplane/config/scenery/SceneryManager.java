package com.ogerardin.xplane.config.scenery;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Slf4j
public class SceneryManager {

    @NonNull
    final Path customSceneryFolder;

    @Getter(lazy = true)
    private final List<SceneryPackage> packages = loadPackages();

    private List<SceneryPackage> loadPackages() {
        try (Stream<Path> pathStream = Files.list(customSceneryFolder)) {
                return pathStream.filter(Files::isDirectory)
                    .map(this::getSceneryPackage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Custom Scenery folder not found: {}", customSceneryFolder);
            return Collections.emptyList();
        }
    }

    private SceneryPackage getSceneryPackage(Path folder) {
        return new SceneryPackage(folder);
    }


}
