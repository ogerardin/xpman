package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.PredicateInspection;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.InstallType;

/**
 * This inspection will check that the actual type of installation matches the specified type (if it is not null).
 */
public class CheckInstallTypeMatches extends PredicateInspection<ArchiveInstallSource> {

    public CheckInstallTypeMatches(InstallType requiredInstallType) {
        super(
                installSource -> (requiredInstallType == null)
                        || (requiredInstallType == installSource.getInstallType().orElse(null)),
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("Was expecting type: " + requiredInstallType)
                        .abort(true) // abort following inspections
                        .build());
    }

}
