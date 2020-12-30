package com.ogerardin.xplane.config.install.inspections;

import com.ogerardin.xplane.config.install.InstallableArchive;
import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

/**
 * A {@link CheckInspection} that produces an aborting error if the source zip is not a valid archive.
 */
public class CheckIsValidArchive extends CheckInspection<InstallableArchive> {

    public CheckIsValidArchive() {
        super(
                InstallableArchive::isValidArchive,
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("File is not a valid zip archive")
                        .abort(true) // abort following inspections
                        .build());
    }
}