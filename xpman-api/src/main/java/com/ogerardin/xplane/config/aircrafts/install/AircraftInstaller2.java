package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.util.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

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
public class AircraftInstaller2 {

    public List<InspectionMessage> checkZip(XPlaneInstance xPlaneInstance, Path zipFile) {
        Path aircraftFolder = xPlaneInstance.getAircraftManager().getAircraftFolder();

        Inspections<InstallableZip> inspections = Inspections.of(
                new CheckValidZip(),
                new CheckHasFilesWithType(".acf"),
                new CheckDoesNotOverwritesFiles(aircraftFolder),
                new CheckSingleRootFolder()
        );

        var installableZip = new InstallableZip(zipFile);
        return inspections.apply(installableZip, xPlaneInstance);
    }

    public void installZip(XPlaneInstance xPlaneInstance, Path zipFile) throws IOException {
        FileUtils.unzip(zipFile, xPlaneInstance.getAircraftManager().getAircraftFolder());
    }
}
