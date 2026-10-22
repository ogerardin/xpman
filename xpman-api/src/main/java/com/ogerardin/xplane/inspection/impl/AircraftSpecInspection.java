package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import lombok.NonNull;

/** Inspection that warns if an aircraft's ACF spec version mismatches the X-Plane major version. */
public enum AircraftSpecInspection implements Inspection<Aircraft> {

    INSTANCE;

    @Override
    public InspectionResult inspect(@NonNull Aircraft aircraft) {
        String acfSpec = aircraft.getAcfFile().getFileSpecVersion();
        String xPlaneSpec = aircraft.getXPlane().getMajorVersion().specString();
        return switch (acfSpec.compareTo(xPlaneSpec)) {
            case -1 -> InspectionResult.of(InspectionMessage.warn(
                    "Aircraft is intended for a previous version of X-Plane: " + acfSpec));
            case 1 -> InspectionResult.of(InspectionMessage.error(
                    "Aircraft is intended for a later version of X-Plane: " + acfSpec));
            default -> InspectionResult.empty();
        };
    }
}
