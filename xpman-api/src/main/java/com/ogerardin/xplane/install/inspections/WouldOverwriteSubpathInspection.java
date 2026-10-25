package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.zip.Archive;
import com.ogerardin.xplane.util.zip.DryRunArchive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * An {@link Inspection} that warns when installing the given {@link Archive} subpath into the
 * given target folder would overwrite existing files — the subpath counterpart of
 * {@link WouldOverwriteInspection}, for archives (like openSAM's) that install only part of
 * themselves into a given target.
 */
@RequiredArgsConstructor
public class WouldOverwriteSubpathInspection implements Inspection<Archive> {

    private final Path targetFolder;
    private final Path subpath;

    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        DryRunArchive dryRun = new DryRunArchive(archive);
        dryRun.extract(targetFolder, subpath, (_, _) -> {});
        List<Path> overwrites = dryRun.overwrites();
        if (overwrites.isEmpty()) {
            return InspectionResult.empty();
        }
        return InspectionResult.of(InspectionMessage.warn(
                "Installing will overwrite %d existing file(s), e.g. %s"
                        .formatted(overwrites.size(), overwrites.getFirst())));
    }
}
