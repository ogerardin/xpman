package com.ogerardin.xplane.config.aircrafts;

import com.ogerardin.xplane.config.aircrafts.custom.CustomAircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.FileUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    private final List<Class<?>> aircraftClasses = findAircraftClasses();

    public AircraftManager(Path aircraftFolder) {
        this.aircraftFolder = aircraftFolder;
        this.disabledAircraftFolder = aircraftFolder.getParent().resolve(aircraftFolder.getFileName() + " (disabled)");
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    private List<Aircraft> loadAircrafts() {
        // find all .acf files under the Aircrafts folder
        Predicate<Path> predicate = f -> f.getFileName().toString().endsWith(".acf");
        List<Path> acfFiles = FileUtils.findFiles(aircraftFolder, predicate);
        log.debug("Found {} acf files", acfFiles.size());
        List<Path> disabledAcfFiles = FileUtils.findFiles(disabledAircraftFolder, predicate);
        log.debug("Found {} disabled acf files", disabledAcfFiles.size());
        List<Path> allAcfFiles = Stream.of(acfFiles, disabledAcfFiles).flatMap(Collection::stream).collect(Collectors.toList());

        // build Aircraft object for each applicable file
        Predicate<AcfFile> isVersion11 = acf -> acf.getFileSpecVersion().matches("11(\\d\\d)");
        List<Aircraft> aircrafts = allAcfFiles.parallelStream()
                .map(AcfFile::new)
                .filter(isVersion11)
                .map(this::getAircraft)
                .peek(aircraft -> aircraft.setEnabled(aircraft.getAcfFile().getFile().startsWith(aircraftFolder)))
                .collect(Collectors.toList());
        log.debug("Loaded {} aircrafts", aircrafts.size());
        return aircrafts;
    }

    private Aircraft getAircraft(AcfFile acfFile) {
        for (Class<?> aircraftClass : aircraftClasses) {
            try {
                Constructor<?> constructor = aircraftClass.getConstructor(AcfFile.class);
                Aircraft aircraft = (Aircraft) constructor.newInstance(acfFile);
                log.info("Found known custom aircraft {} in {}", aircraftClass.getSimpleName(), acfFile.getFile());
                return aircraft;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
//                log.debug("Failed to instantiate {} for {}: {}", aircraftClass, acfFile, e.toString());
            }
        }
        return new Aircraft(acfFile);
    }

    private List<Class<?>> findAircraftClasses() {
        String customAircraftsPackageName = CustomAircraft.class.getPackage().getName();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .whitelistPackages(customAircraftsPackageName)
                .scan()
        ) {
            ClassInfoList classInfoList = scanResult.getSubclasses(Aircraft.class.getName());
            List<Class<?>> classes = classInfoList.loadClasses();
            log.info("Custom aircraft classes found: {}", classes);
            return classes;
        }
    }

}
