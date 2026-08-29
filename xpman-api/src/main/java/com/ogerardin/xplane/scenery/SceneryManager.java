package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.IllegalOperation;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
import com.ogerardin.xplane.file.data.scenery.PathSceneryPackIniItem;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

/**
 * The scenery library: one {@link SceneryEntry} per scenery_packs.ini entry (in file order,
 * {@link SceneryEntry#getRank() rank} = 1-based position in the file), followed by on-disk
 * folders not listed in the ini.
 */
@Slf4j
@ToString
public class SceneryManager extends Manager<SceneryEntry> implements InstallTarget {

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
     * Returns an unmodifiable list of all scenery entries (one per scenery_packs.ini entry in
     * file order, then on-disk folders not listed in the ini). Triggers a synchronous load if needed.
     */
    public List<SceneryEntry> getSceneryEntries() {
        if (items == null) {
            loadPackages();
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns an unmodifiable list of the {@link SceneryPackage} instances of all entries that
     * have one, in list order.
     */
    public List<SceneryPackage> getSceneryPackages() {
        return getSceneryEntries().stream()
                .map(SceneryEntry::getSceneryPackage)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Trigger an asynchronous reload of the scenery entries.
     */
    public void reload() {
        AsyncHelper.runAsync(this::loadPackages);
    }

    @SneakyThrows
    public void loadPackages() {
        log.info("Loading scenery entries...");
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADING).source(this).build());

        items = buildEntries();

        log.info("Loaded {} scenery entries", items.size());
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADED).source(this).items(items).build());
    }

    private List<SceneryEntry> buildEntries() {
        // on-disk packages by folder (global scenery first, then custom scenery, each sorted by name)
        Map<Path, SceneryPackage> packagesByFolder = new LinkedHashMap<>();
        collectPackages(globalSceneryFolder, packagesByFolder);
        collectPackages(sceneryFolder, packagesByFolder);

        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        List<SceneryPackIniItem> iniItems = iniFile == null ? List.of() : iniFile.getSceneryPackList();

        List<SceneryEntry> entries = new ArrayList<>();
        Set<Path> resolvedFolders = new HashSet<>();

        for (int i = 0; i < iniItems.size(); i++) {
            SceneryPackIniItem item = iniItems.get(i);
            int rank = i + 1;
            Path resolvedFolder = item.resolveFolder(xPlane.getBaseFolder(), xPlane.getPaths().globalAirports());
            if (resolvedFolder != null) {
                resolvedFolders.add(resolvedFolder);
            }
            SceneryPackage sceneryPackage = resolvedFolder == null ? null : packagesByFolder.get(resolvedFolder);
            if (sceneryPackage == null && item instanceof PathSceneryPackIniItem pathItem) {
                // fallback: legacy enable/disable moves the folder into the disabled scenery
                // folder without touching the ini; look for it there
                Path disabledFolder = disabledSceneryFolder.resolve(pathItem.getFolder().getFileName());
                if (Files.isDirectory(disabledFolder)) {
                    sceneryPackage = createSceneryPackage(disabledFolder);
                }
            }
            if (sceneryPackage != null) {
                sceneryPackage.setRank(rank);
                entries.add(SceneryEntry.inIni(item, sceneryPackage, rank));
            } else {
                entries.add(SceneryEntry.unresolved(item, rank));
            }
        }

        // append on-disk packages that are not listed in the ini
        packagesByFolder.forEach((folder, sceneryPackage) -> {
            if (!resolvedFolders.contains(folder)) {
                entries.add(SceneryEntry.notListed(sceneryPackage));
            }
        });

        return entries;
    }

    private SceneryPacksIniFile getSceneryPacksIniFile() {
        final Path sceneryPacksIniFile = sceneryFolder.resolve("scenery_packs.ini");
        return Files.exists(sceneryPacksIniFile) ?
                new SceneryPacksIniFile(sceneryPacksIniFile) : null;
    }

    @SneakyThrows
    private void collectPackages(Path sceneryFolder, Map<Path, SceneryPackage> packagesByFolder) {
        if (!Files.isDirectory(sceneryFolder)) {
            return;
        }
        try (var stream = Files.list(sceneryFolder)) {
            stream.filter(Files::isDirectory)
                    .sorted()
                    .map(this::createSceneryPackage)
                    .forEach(pkg -> packagesByFolder.put(pkg.getFolder(), pkg));
        }
    }

    @SneakyThrows
    private SceneryPackage createSceneryPackage(Path folder) {
        SceneryPackage sceneryPackage = IntrospectionHelper.getBestSubclassInstance(SceneryPackage.class, folder);
        sceneryPackage.setEnabled(isLocatedInAuthorizedBase(sceneryPackage.getFolder()));
        return sceneryPackage;
    }

    private boolean isLocatedInAuthorizedBase(Path folder) {
        return folder.startsWith(sceneryFolder) || folder.startsWith(globalSceneryFolder);
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
        if (!isEnabled(sceneryPackage)) {
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
        sceneryPackage.setEnabled(isLocatedInAuthorizedBase(sceneryPackage.getFolder()));
    }

    @SneakyThrows
    public void moveSceneryPackageToTrash(SceneryPackage sceneryPackage) {
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(sceneryPackage.getFolder().toFile());
    }

    @Override
    public void install(Archive archive, ProgressListener progressListener) throws IOException {
        archive.extract(getSceneryFolder(), progressListener);
        reload();
    }

}
