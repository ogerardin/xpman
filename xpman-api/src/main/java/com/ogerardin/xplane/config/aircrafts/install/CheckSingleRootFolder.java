package com.ogerardin.xplane.config.aircrafts.install;

import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.FileUtils;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckSingleRootFolder extends CheckInspection<InstallableZip> {

    public CheckSingleRootFolder() {
        super(
                zip -> countRootFolders(zip) == 1,
                () -> InspectionMessage.builder()
                        .severity(Severity.ERROR)
                        .message("This file cannot be installed automatically because it " +
                                "does not contain a single folder; please check instructions and install manually.")
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
