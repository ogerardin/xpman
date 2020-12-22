package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link CheckInspection} that produces an error if the source zip does not contain any file with the specified
 * suffix.
 */
@Data
public class CheckHasFilesWithType2 implements Inspection<InstallableZip> {

    private final String suffix;

    @Override
    public List<InspectionMessage> apply(InstallableZip zip) {
        List<InspectionMessage> messages = zip.getPaths().stream()
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .map(path -> InspectionMessage.builder()
                        .severity(Severity.INFO).message("Found " + path)
                        .build())
                .collect(Collectors.toList());
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
