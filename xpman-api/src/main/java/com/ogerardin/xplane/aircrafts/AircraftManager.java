package com.ogerardin.xplane.aircrafts;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.IllegalOperation;
import com.ogerardin.xplane.XPlaneInstance;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AircraftManager extends Manager<Aircraft> {

    @NonNull
    @Getter
    private final Path aircraftFolder;

    @NonNull
    @Getter
    private final Path disabledAircraftFolder;

    public AircraftManager(@NonNull XPlaneInstance xPlaneInstance, @NonNull Path aircraftFolder) {
        super(xPlaneInstance);
        this.aircraftFolder = aircraftFolder;
        this.disabledAircraftFolder = aircraftFolder.resolveSibling(aircraftFolder.getFileName() + " (disabled)");
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    public List<Aircraft> loadAircrafts() {
        // find all .acf files under the Aircrafts folder
        Predicate<Path> isAcfPredicate = f -> f.getFileName().toString().endsWith(".acf");
        List<Path> acfFiles = FileUtils.findFiles(aircraftFolder, isAcfPredicate);
        log.info("Found {} acf files", acfFiles.size());
        List<Path> disabledAcfFiles = FileUtils.findFiles(disabledAircraftFolder, isAcfPredicate);
        log.info("Found {} disabled acf files", disabledAcfFiles.size());
        List<Path> allAcfFiles = Stream.of(acfFiles, disabledAcfFiles).flatMap(Collection::stream).collect(Collectors.toList());

        // build Aircraft object for each applicable file
        Predicate<AcfFile> isVersion11 = acf -> acf.getFileSpecVersion().matches("11(\\d\\d)");
        List<Aircraft> aircrafts = allAcfFiles.parallelStream()
                .map(AcfFile::new)
                .filter(isVersion11)
                .map(this::getAircraft)
                .collect(Collectors.toList());
        log.info("Loaded {} aircrafts", aircrafts.size());
        return aircrafts;
    }

    @SneakyThrows
    private Aircraft getAircraft(AcfFile acfFile) {
        Aircraft aircraft = IntrospectionHelper.getBestSubclassInstance(Aircraft.class, acfFile);
        aircraft.setEnabled(isEnabled(aircraft));
        return aircraft;
    }

    private boolean isEnabled(Aircraft aircraft) {
        return aircraft.getAcfFile().getFile().startsWith(aircraftFolder);
    }

    @SneakyThrows
    public void enableAircraft(Aircraft aircraft) {
        if (isEnabled(aircraft)) {
            throw new IllegalOperation("Aircraft already enabled");
        }
        moveAircraft(aircraft, aircraftFolder);
    }

    @SneakyThrows
    public void disableAircraft(Aircraft aircraft) {
        if (! isEnabled(aircraft)) {
            throw new IllegalOperation("Aircraft already disabled");
        }
        moveAircraft(aircraft, disabledAircraftFolder);
    }

    private void moveAircraft(Aircraft aircraft, Path targetFolder) throws IOException {
        // make sure target directory exists
        Files.createDirectories(targetFolder);

        Path acfFile = aircraft.getAcfFile().getFile();
        // move the folder containing the .acf file...
        Path sourceFolder = acfFile.getParent();
        // ...to the target folder, keeping the original folder name
        Path target = targetFolder.resolve(sourceFolder.getFileName());
        Files.move(sourceFolder, target);

        // update aircraft
        Path newAcfFile = target.resolve(acfFile.getFileName());
        aircraft.setAcfFile(new AcfFile(newAcfFile));
        aircraft.setEnabled(isEnabled(aircraft));
    }

    @SneakyThrows
    public void moveAircraftToTrash(Aircraft aircraft) {
        Path acfFile = aircraft.getAcfFile().getFile();
        // move the folder containing the .acf file...
        Path folder = acfFile.getParent();
        // ...to the trash
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(new File[]{folder.toFile()});
    }

}
