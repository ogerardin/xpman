package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.config.scenery.SceneryPackage;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum InstallType {
    AIRCRAFT,
    SCENERY;

    public static Set<InstallType> candidateTypes(InstallableArchive zip) {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Map<InstallType, List<Path>> pathListBySuffix = zip.getPaths().stream()
                .filter(path -> getInstallTypeMarker(path).isPresent())
                .collect(Collectors.groupingBy(path -> getInstallTypeMarker(path).get()));

        return pathListBySuffix.keySet();
    }

    private static Optional<InstallType> getInstallTypeMarker(Path path) {
        if (path.getFileName().toString().endsWith(".acf")) {
            return Optional.of(InstallType.AIRCRAFT);
        }
        if (path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                && path.getNameCount() == 2) {
            return Optional.of(InstallType.SCENERY);
        }
        return Optional.empty();
    }

}
