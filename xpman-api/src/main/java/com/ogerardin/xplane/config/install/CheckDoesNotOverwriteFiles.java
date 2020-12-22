package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link CheckInspection} that produces a warning if the source zip would overwrite existing file(s)
 * if extracted into the target folder.
 */
public class CheckDoesNotOverwriteFiles extends CheckInspection<InstallableZip> {

    public CheckDoesNotOverwriteFiles(Path targetFolder) {
        super(
                zip -> zip.getPaths().stream().noneMatch(path -> Files.exists(targetFolder.resolve(path))),
                () -> InspectionMessage.builder().
                        severity(Severity.WARN)
                        .message("some files in the archive will overwrite existing files! Only proceed if you are updating")
                        .build()
        );
    }
}
