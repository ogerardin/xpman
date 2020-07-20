package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

public class CheckHasFilesWithType extends CheckInspection<InstallableZip> {

    public CheckHasFilesWithType(String suffix) {
        super(
                (zip, xPlaneInstance) -> zip.getPaths().stream().anyMatch(path -> path.getFileName().toString().endsWith(suffix)),
                () -> new InspectionMessage(Severity.ERROR, "No files with suffix " + suffix + " found in ZIP")
        );
    }

}
