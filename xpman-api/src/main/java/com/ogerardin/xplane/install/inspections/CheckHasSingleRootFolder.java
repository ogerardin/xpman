package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.install.InstallableArchive;
import com.ogerardin.xplane.inspection.CheckInspection;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link CheckInspection} that produces an error if the source zip contains more than one root folder
 */
public class CheckHasSingleRootFolder implements Inspection<InstallableArchive> {

    @SneakyThrows
    private static Set<Path> rootFolders(InstallableArchive zip) {
        return zip.getPaths().stream()
                .map(path -> path.getName(0))
                .collect(Collectors.toSet());
    }

    @Override
    public List<InspectionMessage> inspect(InstallableArchive target) {
        Set<Path> rootFolders = rootFolders(target);
        if (rootFolders.size() != 1) {
            return Collections.singletonList(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("This file cannot be installed automatically because it " +
                            "does not contain a single root folder; please check instructions and install manually.")
                    .abort(true)
                    .build());
        }
        return Collections.singletonList(InspectionMessage.builder()
                .severity(Severity.INFO)
                .message("Root folder: " + rootFolders.iterator().next())
                .build());

    }
}
