package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;

/**
 * An inspection that checks if an archive contains a Skunkcrafts updater configuration file.
 * Returns an INFO message if the archive is Skunkcrafts-updatable.
 */
public enum SkunkcraftsUpdatableInspection implements Inspection<Archive> {

    INSTANCE;

    private static final String SKUNKCRAFTS_CFG = "skunkcrafts_updater.cfg";

    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        boolean hasSkunkcraftsCfg = archive.getPaths().stream()
                .anyMatch(path -> path.getFileName().toString().equals(SKUNKCRAFTS_CFG));

        if (hasSkunkcraftsCfg) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.INFO)
                    .message("This addon supports Skunkcrafts Updater")
                    .build());
        }

        return InspectionResult.empty();
    }
}
