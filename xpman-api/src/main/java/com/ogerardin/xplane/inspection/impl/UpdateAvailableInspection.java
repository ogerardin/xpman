package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This {@link Inspection} produces WARN messages when updates are available for the target.
 *
 * <p>Skunkcrafts-updatable targets are judged by content truth (the CRC diff per channel),
 * with a separate message per channel; targets without a Skunkcrafts config that supply a
 * custom publication channel (e.g. a web-scraped version) keep the version-based message.</p>
 */
public enum UpdateAvailableInspection implements Inspection<SkunkcraftsUpdatable> {

    INSTANCE;

    @Override
    public @NonNull InspectionResult inspect(@NonNull SkunkcraftsUpdatable target) {
        if (target.isSkunkcraftsUpdatable()) {
            List<InspectionMessage> messages = new ArrayList<>();
            if (target.isSkunkcraftsUpdateAvailable()) {
                messages.add(updateMessage(target, "Update available", target.getSkunkcraftsLatestVersion()));
            }
            if (target.isSkunkcraftsBetaUpdateAvailable()) {
                messages.add(updateMessage(target, "Beta update available", target.getSkunkcraftsBetaLatestVersion()));
            }
            return InspectionResult.of(messages);
        }

        // custom publication channel without a Skunkcrafts cfg: version comparison is all there is
        String latest = target.getLatestVersion();
        if (latest == null || Objects.equals(target.getInstalledVersion(), latest)) {
            return InspectionResult.empty();
        }
        return InspectionResult.of(updateMessage(target, "Update available", latest));
    }

    private static InspectionMessage updateMessage(SkunkcraftsUpdatable target, String text, String label) {
        return InspectionMessage.builder()
                .severity(Severity.WARN)
                .object(target.getName())
                .message(label != null ? text + ": " + label : text)
                .build();
    }
}
