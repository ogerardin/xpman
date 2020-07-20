package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.util.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ogerardin.xplane.inspection.Severity.*;

@UtilityClass
@Slf4j
public class AircraftInstaller {

    public InspectionMessage checkZip(XPlaneInstance xPlaneInstance, Path zipFile) {
        Path aircraftFolder = xPlaneInstance.getAircraftManager().getAircraftFolder();

        List<String> acfFiles = new ArrayList<>();
        boolean overwriting = false;
        Set<Path> rootPaths = new HashSet<>();
        try {
            List<Path> paths = FileUtils.zipPaths(zipFile).collect(Collectors.toList());
            for (Path path : paths) {
                rootPaths.add(path.getName(0));
                if (path.getFileName().toString().endsWith(".acf")) {
                    log.debug("Found acf file: {}", path);
                    acfFiles.add(path.toString());
                }
                if (Files.exists(aircraftFolder.resolve(path))) {
                    overwriting = true;
                }
            }
        } catch (IOException e) {
            return new InspectionMessage(ERROR, "File is not a ZIP archive");
        }
        if (acfFiles.isEmpty()) {
            return new InspectionMessage(ERROR, "No ACF file found in archive");
        }
        if (rootPaths.size() != 1) {
            return new InspectionMessage(ERROR, "This file cannot be installed automatically because it " +
                    "does not contain a single folder; please check instructions and install manually.");
        }
        String message = "Found the following ACF files in archive: " + acfFiles + ".";
        if (overwriting) {
            return new InspectionMessage(WARN, message
                    + "\nWarning: some files in the archive will overwrite existing files! Only proceed if you are updating an aircraft.");
        }
        return new InspectionMessage(OK, message);
    }

    public void installZip(XPlaneInstance xPlaneInstance, Path zipFile) throws IOException {
        FileUtils.unzip(zipFile, xPlaneInstance.getAircraftManager().getAircraftFolder());
    }
}
