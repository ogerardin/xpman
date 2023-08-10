package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.ArchiveInstallSource;
import com.ogerardin.xplane.install.InstallType;
import lombok.NonNull;

import java.util.Set;

/**
 * This inspection will check that the archive is identified as a source for one and only one type of installation
 * (aircraft, scenery, ...)) and produce corresponding {@link InspectionMessage}s:
 */

public enum CheckInstallTypeIdentified implements Inspection<ArchiveInstallSource> {

    INSTANCE;

    @Override
    public @NonNull InspectionResult inspect(@NonNull ArchiveInstallSource installSource) {

        Set<InstallType> candidateTypes = installSource.getCandidateTypes();

        if (candidateTypes.isEmpty()) {
            return InspectionResult.of(
                    InspectionMessage.builder()
                            .severity(Severity.ERROR)
                            .message("Archive type could not be identified. " +
                                    "Either file is not an installable X-Plane add-on, or it is corrupt.")
                            .abort(true)
                            .build()
            );
        }

        if (candidateTypes.size() > 1) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR).message("Mixed content found: " + candidateTypes + ". " +
                            "File can't be installed automatically, check its documentation.")
                    .abort(true)
                    .build()
            );
        }

        //noinspection OptionalGetWithoutIsPresent
        InstallType actualInstallType = installSource.getInstallType().get();

        return InspectionResult.of(
                InspectionMessage.builder()
                        .severity(Severity.INFO)
                        .message("Archive type identified as: " + actualInstallType)
                        .build());
    }
}
