package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

/**
 * A {@link CheckInspection} that produces an error if the source zip does not contain any file with the specified
 * suffix.
 */
public class CheckHasFilesWithType extends CheckInspection<InstallableZip> {

    public CheckHasFilesWithType(String suffix) {
        super(
                zip -> zip.getPaths().stream().anyMatch(path -> path.getFileName().toString().endsWith(suffix)),
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR).message("No files with suffix " + suffix + " found in ZIP")
                        .build()
        );
    }

}
