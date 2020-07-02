package com.ogerardin.xplane.config.scenery;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SceneryManager {

    @NonNull
    final Path customSceneryFolder;

    @Getter(lazy = true)
    private final List<SceneryPackage> packages = loadPackages();

    @SneakyThrows
    private List<SceneryPackage> loadPackages() {
        List<SceneryPackage> packages = Files.list(customSceneryFolder)
                .filter(Files::isDirectory)
                .map(this::getSceneryPackage)
                .collect(Collectors.toList());
        return packages;
    }

    private SceneryPackage getSceneryPackage(Path folder) {
        return new SceneryPackage(folder);
    }


}
