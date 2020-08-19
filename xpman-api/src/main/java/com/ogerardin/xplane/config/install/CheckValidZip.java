package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

/**
 * A {@link CheckInspection} that produces an aborting error if the source zip is not a valid archive.
 */
public class CheckValidZip extends CheckInspection<InstallableZip> {

    public CheckValidZip() {
        super(
                InstallableZip::isValidZip,
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("File is not a valid zip archive")
                        .abort(true) // abort following inspections
                        .build());
    }
}
