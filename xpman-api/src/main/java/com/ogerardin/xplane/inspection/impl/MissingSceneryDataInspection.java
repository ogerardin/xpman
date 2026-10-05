package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * This {@link Inspection} flags scenery packages whose folder contains no recognizable
 * scenery data (no apt.dat, no .dsf tiles, no library.txt, no .obj files) — i.e. broken
 * or empty packages that X-Plane will ignore.
 */
public enum MissingSceneryDataInspection implements Inspection<SceneryPackage> {

    INSTANCE;

    @SneakyThrows
    @Override
    public @NonNull InspectionResult inspect(@NonNull SceneryPackage target) {
        boolean hasAirport = target.getHasAirport();
        boolean hasTiles = target.getTileCount() > 0;
        boolean hasObjects = target.getObjCount() > 0;
        boolean isLibrary = target.isLibrary();
        if (hasAirport || hasTiles || hasObjects || isLibrary) {
            return InspectionResult.empty();
        }
        return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.ERROR)
                .object(target.getName())
                .message("No scenery data found (no apt.dat, no .dsf tiles, no library.txt, no .obj files)")
                .build());
    }
}