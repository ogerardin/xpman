package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.util.FileUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@UtilityClass
@Slf4j
public class AircraftInstaller {

    public List<InspectionMessage> checkZip(XPlaneInstance xPlaneInstance, Path zipFile) {
        Path aircraftFolder = xPlaneInstance.getAircraftManager().getAircraftFolder();

        Inspections<InstallableZip> inspections = Inspections.of(
                new CheckValidZip(),
                new CheckHasFilesWithType(".acf"),
                new CheckDoesNotOverwriteFiles(aircraftFolder),
                new CheckSingleRootFolder()
        );

        var installableZip = new InstallableZip(zipFile);
        return inspections.apply(installableZip);
    }

    public void installZip(XPlaneInstance xPlaneInstance, Path zipFile) throws IOException {
        FileUtils.unzip(zipFile, xPlaneInstance.getAircraftManager().getAircraftFolder());
    }
}
