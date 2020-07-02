package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.install.CheckResult.Status;
import com.ogerardin.xplane.util.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class AircraftInstaller {

    public CheckResult checkZip(XPlaneInstance xPlaneInstance, Path zipFile) {
        //TODO: assert single folder

        Path aircraftFolder = xPlaneInstance.getAircraftManager().getAircraftFolder();

        List<String> acfFiles = new ArrayList<>();
        boolean overwriting = false;
        try {
            List<Path> paths = FileUtils.zipPaths(zipFile).collect(Collectors.toList());
            for (Path path : paths) {
                if (path.getFileName().toString().endsWith(".acf")) {
                    log.debug("Found acf file: {}", path);
                    acfFiles.add(path.toString());
                }
                if (Files.exists(aircraftFolder.resolve(path))) {
                    overwriting = true;
                }
            }
        } catch (IOException e) {
            return new CheckResult(Status.ERROR, "File is not a ZIP archive");
        }
        if (acfFiles.isEmpty()) {
            return new CheckResult(Status.ERROR, "No ACF file found in archive");
        }
        String message = "Found the following ACF files in archive: " + acfFiles + ".";
        if (overwriting) {
            return new CheckResult(Status.WARN, message
                    + "\nWarning: some files in ythe archive will overwrite existing files! Only proceed if you are updating an aircraft.");
        }
        return new CheckResult(Status.OK, message);
    }

    public void installZip(XPlaneInstance xPlaneInstance, Path zipFile) throws IOException {
        FileUtils.unzip(zipFile, xPlaneInstance.getAircraftManager().getAircraftFolder());
    }
}