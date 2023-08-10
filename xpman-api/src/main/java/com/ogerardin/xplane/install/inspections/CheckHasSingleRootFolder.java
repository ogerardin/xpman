package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.*;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link PredicateInspection} that produces an error if the source zip contains more than one root folder
 */
public enum CheckHasSingleRootFolder implements Inspection<Archive> {

    INSTANCE;

    @SneakyThrows
    private static Set<Path> rootFolders(Archive zip) {
        return zip.getPaths().stream()
                .map(path -> path.getName(0))
                .collect(Collectors.toSet());
    }

    @Override
    public InspectionResult inspect(@NonNull Archive target) {
        Set<Path> rootFolders = rootFolders(target);
        if (rootFolders.size() != 1) {
            return InspectionResult.of(InspectionMessage.builder()
                    .severity(Severity.ERROR)
                    .message("This file cannot be installed automatically because it " +
                            "does not contain a single root folder; please check instructions and install manually.")
                    .abort(true)
                    .build());
        }
        return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.INFO)
                .message("Root folder: " + rootFolders.iterator().next())
                .build());

    }
}
