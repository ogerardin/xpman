package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.IllegalOperation;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.Installer;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SceneryManager extends Manager<SceneryPackage> implements InstallTarget {

    @NonNull
    @Getter
    private final Path sceneryFolder;

    @Getter(lazy = true)
    private final SceneryPacksIniFile sceneryPacksIniFile = loadSceneryPacksIniFile();

    public SceneryManager(@NonNull XPlane xPlane, @NonNull Path sceneryFolder) {
        super(xPlane);
        this.sceneryFolder = sceneryFolder;
    }

    @SneakyThrows
    public List<SceneryPackage> loadPackages() {
        return getSceneryPackages(sceneryFolder);

    }

    private List<SceneryPackage> getSceneryPackages(Path sceneryFolder) throws IOException {
        if (! Files.exists(sceneryFolder)) {
            return Collections.emptyList();
        }
        return Files.list(sceneryFolder)
                .filter(Files::isDirectory)
                .map(this::getSceneryPackage)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
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

    @SneakyThrows
    public void moveSceneryPackageToTrash(SceneryPackage sceneryPackage) {
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(new File[]{sceneryPackage.getFolder().toFile()});
    }

    @Override
    public void install(InstallableArchive archive, Installer.ProgressListener progressListener) throws IOException {
        archive.installTo(getSceneryFolder(), progressListener);
    }

}
