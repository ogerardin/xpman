package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.IllegalOperation;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SceneryManager extends Manager<SceneryPackage> implements InstallTarget {

    @NonNull
    @Getter
    private final Path sceneryFolder;

    @NonNull
    @Getter
    private final Path disabledSceneryFolder;

    private List<SceneryPackage> sceneryPackages = null;

    public SceneryManager(@NonNull XPlane xPlane, @NonNull Path sceneryFolder) {
        super(xPlane);
        this.sceneryFolder = sceneryFolder;
        this.disabledSceneryFolder = sceneryFolder.resolveSibling(sceneryFolder.getFileName() + " (disabled)");
    }

    /**
     * Returns an unmodifiable list of all Scenery Packages available in the X-Plane folder.
     * If the list has not already been loaded, this method will trigger a synchronous load.
     */
    public List<SceneryPackage> getSceneryPackages() {
        if (sceneryPackages == null) {
            loadPackages();
        }
        return Collections.unmodifiableList(sceneryPackages);
    }

    /**
     * Trigger an asynchronous reload of the scenary package list.
     */
    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadPackages);
    }

    @SneakyThrows
    @Synchronized
    public void loadPackages() {

        fireEvent(new ManagerEvent.Loading<>());

        SceneryPacksIniFile sceneryPacksIniFile = getSceneryPacksIniFile();
        sceneryPackages = Stream.of(
                getSceneryPackages(sceneryFolder, sceneryPacksIniFile),
                getSceneryPackages(disabledSceneryFolder, null)
        ).flatMap(Collection::stream)
                .sorted(Comparator.comparingInt(SceneryPackage::getRank))
                .collect(Collectors.toList());

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
        Files.createDirectories(targetFolder);
        Path target = targetFolder.resolve(sourceFolder.getFileName());
        Files.move(sourceFolder, target);

        // update scenery package
        sceneryPackage.setFolder(target);
        sceneryPackage.setEnabled(isEnabled(sceneryPackage));
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
