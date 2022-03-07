package com.ogerardin.xplane.aircrafts;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class AircraftManager extends Manager<Aircraft> implements InstallTarget {

    @NonNull
    @Getter
    private final Path aircraftFolder;

    private List<Aircraft> aircrafts = null;

    public AircraftManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.aircraftFolder = xPlane.getPaths().aircraft();
    }

    /**
     * Returns an unmodifiable list of all Aircrafts available in the X-Plane folder.
     * If the list has not already been loaded, this method will trigger a synchronous load.
     */
    public List<Aircraft> getAircrafts() {
        if (aircrafts == null) {
            loadAircrafts();
        }
        return Collections.unmodifiableList(aircrafts);
    }

    /**
     * Trigger an asynchronous reload of the aircraft list.
     */
    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadAircrafts);
    }

    @SneakyThrows
    @Synchronized
    private void loadAircrafts() {

        log.info("Loading aircrafts...");
        fireEvent(new ManagerEvent.Loading<>());

        // find all .acf files under the Aircrafts folder
        Predicate<Path> isAcfPredicate = f -> f.getFileName().toString().endsWith(".acf");
        List<Path> acfFiles = FileUtils.findFiles(aircraftFolder, isAcfPredicate);
        log.debug("Found {} acf files", acfFiles.size());

        // build Aircraft object for each applicable file
        Predicate<AcfFile> isVersion11 = acf -> acf.getFileSpecVersion().matches("11(\\d\\d)");
        aircrafts = acfFiles.parallelStream()
                .map(AcfFile::new)
                .filter(isVersion11)
                .map(this::getAircraft)
                .collect(Collectors.toList());

        log.info("Loaded {} aircrafts", aircrafts.size());
        fireEvent(new ManagerEvent.Loaded<>(aircrafts));
    }

    @SneakyThrows
    private Aircraft getAircraft(AcfFile acfFile) {
        return IntrospectionHelper.getBestSubclassInstance(Aircraft.class, acfFile);
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
    public void install(InstallableArchive archive, ProgressListener progressListener) throws IOException {
        archive.installTo(getAircraftFolder(), progressListener);
        reload();
    }
}
