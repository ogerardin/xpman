package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

public class CheckValidZip extends CheckInspection<InstallableZip> {

    public CheckValidZip() {
        super(
                (zip, xPlaneInstance) -> zip.isValidZip(),
                () -> new InspectionMessage(Severity.ERROR, "File is not a valid zip archive"));
    }
}
