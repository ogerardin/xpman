package com.ogerardin.xplane.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.SceneryPacksIniFile;
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

    private List<SceneryPackIniItem> iniItems = new ArrayList<>();
    private Map<Path, SceneryPackage> packagesByFolder = new LinkedHashMap<>();
    @Getter
    private boolean pendingChanges;

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

        packagesByFolder = collectPackages();
        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        iniItems = iniFile == null ? new ArrayList<>() : new ArrayList<>(iniFile.getSceneryPackList());
        pendingChanges = false;
        items = buildEntries();

        log.info("Loaded {} scenery entries", items.size());
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADED).source(this).items(items).build());
    }

    private List<SceneryEntry> buildEntries() {
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
            if (sceneryPackage == null && resolvedFolder != null) {
                // fallback: legacy enable/disable moves the folder into the disabled scenery
                // folder without touching the ini; look for it there
                Path disabledFolder = disabledSceneryFolder.resolve(resolvedFolder.getFileName());
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
    private Map<Path, SceneryPackage> collectPackages() {
        Map<Path, SceneryPackage> result = new LinkedHashMap<>();
        collectPackages(globalSceneryFolder, result);
        collectPackages(sceneryFolder, result);
        return result;
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
        sceneryPackage.setSystem(folder.startsWith(globalSceneryFolder));
        return sceneryPackage;
    }

    private boolean isLocatedInAuthorizedBase(Path folder) {
        return folder.startsWith(sceneryFolder) || folder.startsWith(globalSceneryFolder);
    }

    public boolean enable(SceneryEntry entry) {
        return updateDisabled(entry, false);
    }

    public boolean disable(SceneryEntry entry) {
        return updateDisabled(entry, true);
    }

    private boolean updateDisabled(SceneryEntry entry, boolean disabled) {
        if (entry.getIniItem() != null && entry.getIniItem().isDisabled() != disabled) {
            entry.getIniItem().setDisabled(disabled);
            pendingChanges = true;
            return true;
        }
        return false;
    }

    /**
     * Moves the entry's ini item to the given 0-based index in scenery_packs.ini order
     * (the dragged/dropped entry takes the target slot) and updates ranks accordingly.
     * Does nothing and returns false if the entry is not ini-listed or the index is out of bounds.
     */
    public boolean moveTo(SceneryEntry entry, int targetIndex) {
        int index = indexOf(entry.getIniItem());
        if (index < 0 || index == targetIndex
                || targetIndex < 0 || targetIndex >= iniItems.size()) {
            return false;
        }
        iniItems.add(targetIndex, iniItems.remove(index));
        items = buildEntries();
        pendingChanges = true;
        return true;
    }

    public boolean addToIni(SceneryEntry entry) {
        if (entry.getIniItem() == null && entry.getSceneryPackage() != null
                && !entry.getSceneryPackage().isSystem()) {
            iniItems.add(SceneryPackIniItem.of(iniValue(entry.getSceneryPackage())));
            items = buildEntries();
            pendingChanges = true;
            return true;
        }
        return false;
    }

    public boolean removeFromIni(SceneryEntry entry) {
        if (entry.getIniItem() != null && entry.getSceneryPackage() == null
                && iniItems.remove(entry.getIniItem())) {
            items = buildEntries();
            pendingChanges = true;
            return true;
        }
        return false;
    }

    public void organize(List<SceneryPackage> orderedPackages) {
        Map<SceneryPackage, SceneryPackIniItem> existing = new java.util.IdentityHashMap<>();
        items.stream().filter(entry -> entry.getIniItem() != null && entry.getSceneryPackage() != null)
                .forEach(entry -> existing.put(entry.getSceneryPackage(), entry.getIniItem()));
        iniItems = orderedPackages.stream()
                .map(pkg -> existing.computeIfAbsent(pkg, this::newIniItem))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        changed();
    }

    private SceneryPackIniItem newIniItem(SceneryPackage pkg) {
        return SceneryPackIniItem.of(iniValue(pkg));
    }

    private String iniValue(SceneryPackage pkg) {
        Path folder = pkg.getFolder();
        if (folder.startsWith(disabledSceneryFolder)) {
            folder = sceneryFolder.resolve(folder.getFileName());
        }
        return xPlane.getBaseFolder().relativize(folder).toString();
    }

    private int indexOf(SceneryPackIniItem item) {
        return item == null ? -1 : java.util.stream.IntStream.range(0, iniItems.size())
                .filter(index -> iniItems.get(index) == item).findFirst().orElse(-1);
    }

    private void changed() {
        pendingChanges = true;
        items = buildEntries();
        fireEvent(ManagerEvent.<SceneryEntry>builder().type(LOADED).source(this).items(items).build());
    }

    @SneakyThrows
    public void save() {
        if (!pendingChanges) {
            return;
        }
        SceneryPacksIniFile iniFile = getSceneryPacksIniFile();
        if (iniFile == null) {
            throw new IOException("scenery_packs.ini does not exist");
        }
        iniFile.write(iniFile.getFile(), iniItems);
        pendingChanges = false;
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
