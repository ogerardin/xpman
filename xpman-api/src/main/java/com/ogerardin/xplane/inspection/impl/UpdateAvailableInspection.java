package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.*;
import com.ogerardin.xplane.update.*;
import lombok.NonNull;

/** Reports provider offers and failed discovery without interpreting version labels. */
public enum UpdateAvailableInspection implements Inspection<Updatable> {
    INSTANCE;

    @Override
    public @NonNull InspectionResult inspect(@NonNull Updatable target) {
        var status = target.getUpdateStatus();
        var result = InspectionResult.of(status.offers().stream().map(offer -> message(target,
                (offer.channel() == UpdateChannel.BETA ? "Beta update available" : "Update available")
                        + (offer.version() == null ? "" : ": " + offer.version()))).toList());
        return status.message() == null ? result : result.append(
                InspectionResult.of(message(target, "Could not check updates: " + status.message())));
    }

    private InspectionMessage message(Updatable item, String text) {
        return InspectionMessage.builder().severity(Severity.WARN).object(item.getName()).message(text).build();
    }
}
