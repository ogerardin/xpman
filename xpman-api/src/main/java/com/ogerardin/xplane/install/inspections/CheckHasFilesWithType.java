package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.PredicateInspection;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallableArchive;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * A {@link PredicateInspection} that produces an error if the source zip does not contain any file with the specified
 * suffix.
 */
@Data
public class CheckHasFilesWithType implements Inspection<InstallableArchive> {

    private final String suffix;

    @Override
    public List<InspectionMessage> inspect(InstallableArchive zip) {
        List<InspectionMessage> messages = zip.getPaths().stream()
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .map(path -> InspectionMessage.builder()
                        .severity(Severity.INFO).message("Found " + path)
                        .build())
                .toList();
        if (! messages.isEmpty()) {
            return messages;
        }

        return Collections.singletonList(InspectionMessage.builder()
                        .severity(Severity.ERROR).message("No files with suffix " + suffix + " found in ZIP")
                        .abort(true)
                        .build()
        );

    }
}
