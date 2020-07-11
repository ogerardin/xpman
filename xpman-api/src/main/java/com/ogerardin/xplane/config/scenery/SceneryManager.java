package com.ogerardin.xplane.config.scenery;

import com.ogerardin.xplane.config.IllegalOperation;
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
    private final Path sceneryFolder;

    @NonNull
    private final Path disabledSceneryFolder;


    @Getter(lazy = true)
    private final List<SceneryPackage> packages = loadPackages();

    @Getter(lazy = true)
    private final SceneryPacksIniFile sceneryPacksIniFile = loadSceneryPacksIniFile();

    public SceneryManager(@NonNull Path sceneryFolder) {
        this.sceneryFolder = sceneryFolder;
        this.disabledSceneryFolder = sceneryFolder.resolveSibling(sceneryFolder.getFileName() + " (disabled)");
    }

    private List<SceneryPackage> loadPackages() {
        try (Stream<Path> pathStream = Files.list(sceneryFolder)) {
            return pathStream.filter(Files::isDirectory)
                    .map(this::getSceneryPackage)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Custom Scenery folder not found: {}", sceneryFolder);
            return Collections.emptyList();
        }
    }

    private SceneryPacksIniFile loadSceneryPacksIniFile() {
        final Path sceneryPacksIniFile = sceneryFolder.resolve("scenery_packs.ini");
        return Files.exists(sceneryPacksIniFile) ?
                new SceneryPacksIniFile(sceneryPacksIniFile) : null;
    }

    @SneakyThrows
    private SceneryPackage getSceneryPackage(Path folder) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        sceneryPackage.setEnabled(sceneryPackage.getFolder().startsWith(sceneryFolder));
        // get rank of scenery in scenery_packs.ini
        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        if (iniFile != null) {
            Path sceneryFolder = this.sceneryFolder.getParent().relativize(sceneryPackage.getFolder());
            int index = iniFile.getSceneryPackList().indexOf(sceneryFolder);
            if (index >= 0) {
                sceneryPackage.setRank(index + 1);
            }
        }
        return sceneryPackage;
    }

    private boolean isEnabled(SceneryPackage sceneryPackage) {
        return sceneryPackage.getFolder().startsWith(sceneryFolder);
    }

    @SneakyThrows
    public void enableSceneryPackage(SceneryPackage sceneryPackage) {
        if (isEnabled(sceneryPackage)) {
            throw new IllegalOperation("SceneryPackage already enabled");
        }
        moveSceneryPackage(sceneryPackage, sceneryFolder);
    }

    @SneakyThrows
    public void disableSceneryPackage(SceneryPackage sceneryPackage) {
        if (! isEnabled(sceneryPackage)) {
            throw new IllegalOperation("SceneryPackage already disabled");
        }
        moveSceneryPackage(sceneryPackage, disabledSceneryFolder);
    }

    @SneakyThrows
    private void moveSceneryPackage(SceneryPackage sceneryPackage, Path targetFolder) {
        // move the scenary folder
        Path sourceFolder = sceneryPackage.getFolder();
        // ...to the target folder, keeping the original folder name
        Path target = targetFolder.resolve(sourceFolder.getFileName());
        Files.move(sourceFolder, target);

        // update scenery package
        sceneryPackage.setFolder(target);
        sceneryPackage.setEnabled(isEnabled(sceneryPackage));
    }


}
