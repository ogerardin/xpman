package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.SneakyThrows;

/**
 * A {@link CheckInspection} that produces an error if the source zip contains more than one root folder
 */
public class CheckHasSingleRootFolder extends CheckInspection<InstallableZip> {

    public CheckHasSingleRootFolder() {
        super(
                zip -> countRootFolders(zip) == 1,
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("This file cannot be installed automatically because it " +
                                "does not contain a single folder; please check instructions and install manually.")
                        .abort(true)
                        .build()
        );
    }

    @SneakyThrows
    private static long countRootFolders(InstallableZip zip) {
        return zip.getPaths().stream()
                .map(path -> path.getName(0))
                .distinct()
                .count();
    }

}
