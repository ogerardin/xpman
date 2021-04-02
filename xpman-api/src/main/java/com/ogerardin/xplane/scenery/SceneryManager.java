package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class SceneryManager extends Manager<SceneryPackage> implements InstallTarget {

    @NonNull
    @Getter
    private final Path sceneryFolder;

    private List<SceneryPackage> sceneryPackages = null;

    public SceneryManager(@NonNull XPlane xPlane, @NonNull Path sceneryFolder) {
        super(xPlane);
        this.sceneryFolder = sceneryFolder;
    }

    public List<SceneryPackage> getSceneryPackages() {
        if (sceneryPackages == null) {
            loadPackages();
        }
        return Collections.unmodifiableList(sceneryPackages);
    }

    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadPackages);
    }

    @SneakyThrows
    @Synchronized
    public void loadPackages() {

        fireEvent(new ManagerEvent.Loading<>());

        SceneryPacksIniFile sceneryPacksIniFile = getSceneryPacksIniFile();
        sceneryPackages = getSceneryPackages(sceneryFolder, sceneryPacksIniFile);

        fireEvent(new ManagerEvent.Loaded<>(sceneryPackages));
    }

    private SceneryPacksIniFile getSceneryPacksIniFile() {
        final Path sceneryPacksIniFile = sceneryFolder.resolve("scenery_packs.ini");
        return Files.exists(sceneryPacksIniFile) ?
                new SceneryPacksIniFile(sceneryPacksIniFile) : null;
    }

    private List<SceneryPackage> getSceneryPackages(Path sceneryFolder, SceneryPacksIniFile iniFile) throws IOException {
        if (! Files.exists(sceneryFolder)) {
            return Collections.emptyList();
        }
        return Files.list(sceneryFolder)
                .filter(Files::isDirectory)
                .map(folder -> getSceneryPackage(folder, iniFile))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @SneakyThrows
    private SceneryPackage getSceneryPackage(Path folder, SceneryPacksIniFile iniFile) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        sceneryPackage.setEnabled(sceneryPackage.getFolder().startsWith(sceneryFolder));
        // get rank of scenery in scenery_packs.ini
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
        reload();
    }

}
