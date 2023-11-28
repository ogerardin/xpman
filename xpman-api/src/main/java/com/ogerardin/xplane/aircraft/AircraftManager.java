package com.ogerardin.xplane.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class AircraftManager extends Manager<Aircraft> implements InstallTarget {

    @NonNull
    @Getter
    private final Path aircraftFolder;

    public AircraftManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.aircraftFolder = xPlane.getPaths().aircraft();
    }

    /**
     * Returns an unmodifiable list of all Aircrafts available in the X-Plane folder.
     * If the list has not already been loaded, this method will trigger a synchronous load.
     */
    public List<Aircraft> getAircrafts() {
        if (items == null) {
            loadAircrafts();
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Trigger an asynchronous reload of the aircraft list.
     */
    @Override
    public void reload() {
        AsyncHelper.runAsync(this::loadAircrafts);
    }

    @SneakyThrows
//    @Synchronized
    private void loadAircrafts() {
        Instant t0 = Instant.now();

        log.info("Loading aircraft...");
        fireEvent(ManagerEvent.<Aircraft>builder().source(this).type(LOADING).build());

        // find all .acf files under the Aircraft folder
        Predicate<Path> isAcfPredicate = f -> f.getFileName().toString().endsWith(".acf");
        List<Path> acfFiles = FileUtils.findFiles(aircraftFolder, isAcfPredicate);
        log.debug("Found {} acf files", acfFiles.size());

        // build Aircraft object for each applicable file
        Predicate<AcfFile> isVersion11 = acf -> acf.getFileSpecVersion().matches("11(\\d\\d)");
        items = acfFiles.parallelStream()
                .map(AcfFile::new)
//                .filter(isVersion11)
                .map(this::getAircraft)
                .toList();

        Instant t1 = Instant.now();
        long duration = Duration.between(t0, t1).toMillis();

        log.info("Loaded {} aircraft in {} ms", items.size(), duration);
        fireEvent(ManagerEvent.<Aircraft>builder().type(LOADED).source(this).items(items).build());
    }

    @SneakyThrows
    private Aircraft getAircraft(AcfFile acfFile) {
        return IntrospectionHelper.getBestSubclassInstance(Aircraft.class, xPlane, acfFile);
    }

    @SneakyThrows
    public void moveAircraftToTrash(Aircraft aircraft) {
        Path acfFile = aircraft.getAcfFile().getFile();
        // move the folder containing the .acf file...
        Path folder = acfFile.getParent();
        // ...to the trash
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());
    }

    @Override
    public void install(Archive archive, ProgressListener progressListener) throws IOException {
        archive.extract(getAircraftFolder(), progressListener);
        reload();
    }

    @SneakyThrows
    public void moveLiveryToTrash(Livery livery) {
        Path folder = livery.getAircraft().getLiveriesFolder().resolve(livery.getFolder());
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());

    }

    /**
     * Returns the list of all distinct studios from all available aircraft
     */
    public List<String> getStudios() {
        return getAircrafts().stream().map(Aircraft::getStudio).distinct().toList();
    }
}
