package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import lombok.NonNull;

public enum AircraftSpecInspection implements Inspection<Aircraft> {

    INSTANCE;

    @Override
    public InspectionResult inspect(@NonNull Aircraft aircraft) {
        String acfSpec = aircraft.getAcfFile().getFileSpecVersion();
        String xPlaneSpec = aircraft.getXPlane().getMajorVersion().specString();
        return switch (acfSpec.compareTo(xPlaneSpec)) {
            case -1 -> InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Aircraft is intended for a previous version of X-Plane: " + acfSpec)
                    .build());
            case 1 -> InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Aircraft is intended for a later version of X-Plane: " + acfSpec)
                    .build());
            default -> InspectionResult.empty();
        };
    }
}
