package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

import java.nio.file.Files;
import java.nio.file.Path;

public class CheckDoesNotOverwritesFiles extends CheckInspection<InstallableZip> {

    public CheckDoesNotOverwritesFiles(Path targetFolder) {
        super(
                (zip, xPlaneInstance) -> zip.getPaths().stream().noneMatch(path -> Files.exists(targetFolder.resolve(path))),
                () -> new InspectionMessage(Severity.WARN, "some files in the archive will overwrite existing files! Only proceed if you are updating")
        );
    }
}
