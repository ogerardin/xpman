package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.PredicateInspection;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallableArchive;

/**
 * A {@link PredicateInspection} that produces an aborting error if the source zip is not a valid archive.
 */
public class CheckIsValidArchive extends PredicateInspection<InstallableArchive> {

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
