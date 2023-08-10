package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.*;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A {@link PredicateInspection} that produces an aborting error if the source zip does not contain any file with the specified
 * suffix.
 */
@RequiredArgsConstructor
public class CheckHasFilesWithType implements Inspection<Archive> {

    private final String suffix;

    @Override
    public InspectionResult inspect(@NonNull Archive zip) {
        var messages = zip.getPaths().stream()
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .map(path -> InspectionMessage.builder()
                        .severity(Severity.INFO).message("Found " + path)
                        .build())
                .toList();
        if (! messages.isEmpty()) {
            return InspectionResult.of(messages);
        }

        return InspectionResult.of(InspectionMessage.builder()
                        .severity(Severity.ERROR).message("No files with suffix " + suffix + " found in ZIP")
                        .abort(true)
                        .build()
        );

    }
}
