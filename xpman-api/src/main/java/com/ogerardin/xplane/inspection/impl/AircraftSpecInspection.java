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
        // numeric compare: spec strings are 3-4 digits ("900".."1200"), and String.compareTo
        // returns a char difference (±8 for "900" vs "1200"), not a sign
        int acfSpec = Integer.parseInt(aircraft.getAcfFile().getFileSpecVersion());
        int xPlaneSpec = Integer.parseInt(aircraft.getXPlane().getMajorVersion().specString());
        return switch (Integer.compare(acfSpec, xPlaneSpec)) {
            case -1 -> InspectionResult.of(InspectionMessage.warn(
                    "Aircraft is intended for a previous version of X-Plane: " + acfSpec));
            case 1 -> InspectionResult.of(InspectionMessage.error(
                    "Aircraft is intended for a later version of X-Plane: " + acfSpec));
            default -> InspectionResult.empty();
        };
    }
}
