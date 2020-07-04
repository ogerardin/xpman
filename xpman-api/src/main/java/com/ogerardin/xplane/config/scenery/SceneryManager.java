package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
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

    @Getter(lazy = true)
    private final SceneryPacksIniFile sceneryPacksIniFile = loadSceneryPacksIniFile();


    private List<SceneryPackage> loadPackages() {
        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();

        try (Stream<Path> pathStream = Files.list(customSceneryFolder)) {
            return pathStream.filter(Files::isDirectory)
                    .map(this::getSceneryPackage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Custom Scenery folder not found: {}", customSceneryFolder);
            return Collections.emptyList();
        }
    }

    private SceneryPacksIniFile loadSceneryPacksIniFile() {
        final Path sceneryPacksIniFile = customSceneryFolder.resolve("scenery_packs.ini");
        return Files.exists(sceneryPacksIniFile) ?
                new SceneryPacksIniFile(sceneryPacksIniFile) : null;
    }

    @SneakyThrows
    private SceneryPackage getSceneryPackage(Path folder) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        if (iniFile != null) {
            Path sceneryFolder = customSceneryFolder.getParent().relativize(sceneryPackage.getFolder());
            int index = iniFile.getSceneryPackList().indexOf(sceneryFolder);
            if (index >= 0) {
                sceneryPackage.setRank(index + 1);
            }
        }
        return sceneryPackage;
    }


}
