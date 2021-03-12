package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.NonNull;
import org.apache.commons.lang.WordUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public enum InstallType {
    AIRCRAFT {
        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getAircraftManager();
        }
        @Override
        boolean isMarker(Path path) {
            return path.getFileName().toString().endsWith(".acf");
        }
    },

    SCENERY {
        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getSceneryManager();
        }
        @Override
        boolean isMarker(Path path) {
            return path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                    && path.getNameCount() == 2;
        }
    };

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name());
    }

    public static Set<InstallType> candidateTypes(InstallableArchive zip) {
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Map<InstallType, List<Path>> pathListBySuffix = zip.getPaths().stream()
                .filter(path -> getInstallTypeMarker(path).isPresent())
                .collect(Collectors.groupingBy(path -> getInstallTypeMarker(path).get()));

        return pathListBySuffix.keySet();
    }

    private static Optional<InstallType> getInstallTypeMarker(Path path) {
        return Arrays.stream(InstallType.values())
                .filter(installType -> installType.isMarker(path))
                .findAny();
    }

    abstract InstallTarget target(@NonNull XPlane xPlane);

    abstract boolean isMarker(Path path);
}
