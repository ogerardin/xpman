package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.IllegalOperation;
import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ogerardin.xplane.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class SceneryManager extends Manager<SceneryPackage> implements InstallTarget {

    // a comparator that compares SceneryPackages by rank with nulls last
    public static final Comparator<SceneryPackage> SCENERY_PACKAGE_COMPARATOR = Comparator.comparing(SceneryPackage::getRank, Comparator.nullsLast(Comparator.naturalOrder()));

    @NonNull @Getter
    private final Path sceneryFolder;

    @NonNull @Getter
    private final Path disabledSceneryFolder;

    @NonNull @Getter
    private final Path globalSceneryFolder;

    public SceneryManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.sceneryFolder = xPlane.getPaths().customScenery();
        this.disabledSceneryFolder = xPlane.getPaths().disabledCustomScenery();
        this.globalSceneryFolder = xPlane.getPaths().globalScenery();
    }

    /**
     * Returns an unmodifiable list of all Scenery Packages available in the X-Plane folder.
     * If the list has not already been loaded, this method will trigger a synchronous load.
     */
    public List<SceneryPackage> getSceneryPackages() {
        if (items == null) {
            loadPackages();
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Trigger an asynchronous reload of the scenary package list.
     */
    public void reload() {
        AsyncHelper.runAsync(this::loadPackages);
    }

    @SneakyThrows
//    @Synchronized
    public void loadPackages() {

        log.info("Loading scenery packages...");
        fireEvent(ManagerEvent.<SceneryPackage>builder().type(LOADING).source(this).build());

        SceneryPacksIniFile sceneryPacksIniFile = getSceneryPacksIniFile();
        items = Stream.of(
                getSceneryPackages(globalSceneryFolder, sceneryPacksIniFile),
                getSceneryPackages(sceneryFolder, sceneryPacksIniFile),
                getSceneryPackages(disabledSceneryFolder, null)
        ).flatMap(Collection::stream)
//                .sorted(SCENERY_PACKAGE_COMPARATOR)
                .toList();

        log.info("Loaded {} scenery packages", items.size());
        fireEvent(ManagerEvent.<SceneryPackage>builder().type(LOADED).source(this).items(items).build());
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
        try (Stream<Path> pathStream = Files.list(sceneryFolder)) {
            return pathStream
                    .filter(Files::isDirectory)
                    .map(folder -> getSceneryPackage(folder, iniFile))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        }
    }

    @SneakyThrows
    private SceneryPackage getSceneryPackage(Path folder, SceneryPacksIniFile iniFile) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        sceneryPackage.setEnabled(sceneryPackage.getFolder().startsWith(sceneryFolder));
        // get rank of scenery in scenery_packs.ini
        if (iniFile != null) {
            Path sceneryFolder = this.sceneryFolder.getParent().relativize(sceneryPackage.getFolder());
            PathSceneryPackIniItem item = new PathSceneryPackIniItem(sceneryFolder);
            // Note: *GLOBAL_AIRPORTS* will never be matched as it is a TokenSceneryPackIniItem
            int index = iniFile.getSceneryPackList().indexOf(item);
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
        fileUtils.moveToTrash(sceneryPackage.getFolder().toFile());
    }

    @Override
    public void install(InstallableArchive archive, ProgressListener progressListener) throws IOException {
        archive.installTo(getSceneryFolder(), progressListener);
        reload();
    }

}
