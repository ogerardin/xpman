package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.xplane.config.IllegalOperation;
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

@Data
@Slf4j
public class AircraftManager {

    public final Path aircraftFolder;
    public final Path disabledAircraftFolder;

    @Getter(lazy = true)
    private final List<Aircraft> aircrafts = loadAircrafts();

    @Getter(AccessLevel.NONE)
    private final List<Class<?>> aircraftClasses = IntrospectionHelper.findAllSubclasses(Aircraft.class);

    public AircraftManager(@NonNull Path aircraftFolder) {
        this.aircraftFolder = aircraftFolder;
        this.disabledAircraftFolder = aircraftFolder.getParent().resolve(aircraftFolder.getFileName() + " (disabled)");
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    private List<Aircraft> loadAircrafts() {
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
                .peek(aircraft -> aircraft.setEnabled(aircraft.getAcfFile().getFile().startsWith(aircraftFolder)))
                .collect(Collectors.toList());
        log.info("Loaded {} aircrafts", aircrafts.size());
        return aircrafts;
    }

    @SneakyThrows
    private Aircraft getAircraft(AcfFile acfFile) {
        return IntrospectionHelper.getBestSubclassInstance(Aircraft.class, acfFile);
    }

    @SneakyThrows
    public void enableAircraft(Aircraft aircraft) {
        if (aircraft.getAcfFile().getFile().startsWith(aircraftFolder)) {
            throw new IllegalOperation("Aircraft already enabled");
        }
        moveAircraft(aircraft, aircraftFolder);
    }

    @SneakyThrows
    public void disableAircraft(Aircraft aircraft) {
        if (! aircraft.getAcfFile().getFile().startsWith(aircraftFolder)) {
            throw new IllegalOperation("Aircraft already disabled");
        }
        moveAircraft(aircraft, disabledAircraftFolder);
    }

    private void moveAircraft(Aircraft aircraft, Path targetFolder) throws IOException {
        Path acfFile = aircraft.getAcfFile().getFile();
        // move the folder containing the .acf file...
        Path sourceFolder = acfFile.getParent();
        // ...to the "Aircrafts" folder, keeping the original folder name
        Path target = targetFolder.resolve(sourceFolder.getFileName());
        Files.move(sourceFolder, target);

        // update aircraft
        Path newAcfFile = target.resolve(acfFile.getFileName());
        aircraft.setAcfFile(new AcfFile(newAcfFile));
        aircraft.setEnabled(newAcfFile.startsWith(aircraftFolder));
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
