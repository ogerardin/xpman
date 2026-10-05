package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import lombok.NonNull;

import java.util.Objects;

/**
 * This {@link Inspection} produces a WARN message when a newer version of the target
 * is available (per {@link SkunkcraftsUpdatable#getLatestVersion()}).
 */
public enum UpdateAvailableInspection implements Inspection<SkunkcraftsUpdatable> {

    INSTANCE;

    @Override
    public @NonNull InspectionResult inspect(@NonNull SkunkcraftsUpdatable target) {
        String latest = target.getLatestVersion();
        if (latest == null || Objects.equals(target.getVersion(), latest)) {
            return InspectionResult.empty();
        }
        return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.WARN)
                .object(target.getName())
                .message("Update available: " + latest)
                .build());
    }
}
