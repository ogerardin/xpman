package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.install.InstallableArchive;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
public class CheckIsInstallable implements Inspection<InstallableArchive> {

    private final InstallType requiredInstallType;

    @Override
    public List<InspectionMessage> inspect(InstallableArchive zip) {

        Set<InstallType> candidateTypes = InstallType.candidateTypes(zip);

        if (candidateTypes.isEmpty()) {
            return Collections.singletonList(
                    InspectionMessage.builder()
                            .severity(Severity.ERROR)
                            .message("Archive type could not be identified. " +
                                    "Either file is not an installable X-Plane add-on, or it is corrupt.")
                            .abort(true)
                            .build()
            );
        }

        if (candidateTypes.size() > 1) {
            return Collections.singletonList(InspectionMessage.builder()
                    .severity(Severity.ERROR).message("Mixed content found: " + candidateTypes + ". " +
                            "File can't be installed automatically, check its documentation.")
                    .abort(true)
                    .build()
            );
        }

        InstallType actualInstallType = candidateTypes.iterator().next();

        List<InspectionMessage> messages = new ArrayList<>();
        messages.add(
                InspectionMessage.builder()
                        .severity(Severity.INFO).message("Archive type identified as: " + actualInstallType)
                        .build()
        );

        if (requiredInstallType != null && requiredInstallType != actualInstallType) {
            messages.add(
                    InspectionMessage.builder()
                            .severity(Severity.ERROR).message("Was expecting type: " + requiredInstallType)
                            .abort(true)
                            .build()
            );
        } else {
            // perform InstallType-specific additional inspections
            messages.addAll(actualInstallType.additionalInspections().inspect(zip));
        }

        return messages;

    }

}
