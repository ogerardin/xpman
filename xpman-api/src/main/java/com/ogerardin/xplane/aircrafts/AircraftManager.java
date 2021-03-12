package com.ogerardin.xplane.aircrafts;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.install.Installer;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
public class AircraftManager extends Manager<Aircraft> implements InstallTarget {

    @NonNull
    @Getter
    private final Path aircraftFolder;

    public AircraftManager(@NonNull XPlane xPlane, @NonNull Path aircraftFolder) {
        super(xPlane);
        this.aircraftFolder = aircraftFolder;
    }

    @SuppressWarnings("ConstantConditions")
    @SneakyThrows
    public List<Aircraft> loadAircrafts() {
        // find all .acf files under the Aircrafts folder
        Predicate<Path> isAcfPredicate = f -> f.getFileName().toString().endsWith(".acf");
        List<Path> acfFiles = FileUtils.findFiles(aircraftFolder, isAcfPredicate);
        log.info("Found {} acf files", acfFiles.size());

        // build Aircraft object for each applicable file
        Predicate<AcfFile> isVersion11 = acf -> acf.getFileSpecVersion().matches("11(\\d\\d)");
        List<Aircraft> aircrafts = acfFiles.parallelStream()
                .map(AcfFile::new)
                .filter(isVersion11)
                .map(this::getAircraft)
                .collect(Collectors.toList());
        log.info("Loaded {} aircrafts", aircrafts.size());
        return Collections.unmodifiableList(aircrafts);
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
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(new File[]{folder.toFile()});
    }

    @Override
    public void install(InstallableArchive archive, Installer.ProgressListener progressListener) throws IOException {
        archive.installTo(getAircraftFolder(), progressListener);
    }
}
