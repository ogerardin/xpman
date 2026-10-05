package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * An {@link Inspection} that warns when installing the archive into the given {@link InstallTarget}
 * would overwrite existing files, as determined by the target's {@link InstallTarget#overwrittenFiles dry run}.
 * A dry run that fails means the install itself would fail, which is reported as an error.
 */
@RequiredArgsConstructor
public class WouldOverwriteInspection implements Inspection<Archive> {

    private final InstallTarget target;

    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        try {
            List<Path> overwrites = target.overwrittenFiles(archive);
            if (overwrites.isEmpty()) {
                return InspectionResult.empty();
            }
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Installing will overwrite %d existing file(s), e.g. %s"
                            .formatted(overwrites.size(), overwrites.getFirst()))
                    .build());
        } catch (IOException e) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("Cannot determine what installing would overwrite: " + e.getMessage())
                    .build());
        }
    }
}
