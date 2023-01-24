package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class AircraftSpecInspection implements Inspection<Aircraft> {

    private final XPlane xPlane;

    @Override
    public List<InspectionMessage> inspect(Aircraft aircraft) {
        String acfSpec = aircraft.getAcfFile().getFileSpecVersion();
        String xPlaneSpec = xPlane.getMajorVersion().specString();
        return switch (acfSpec.compareTo(xPlaneSpec)) {
            case -1 -> List.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Aircraft is intended for a previous version of X-Plane: " + acfSpec)
                    .build());
            case 1 -> List.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Aircraft is intended for a later version of X-Plane: " + acfSpec)
                    .build());
            default -> Collections.emptyList();
        };
    }
}
