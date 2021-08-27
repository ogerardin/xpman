package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.NonNull;
import org.apache.commons.lang.WordUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public enum InstallType {
    AIRCRAFT {
        @Override
        boolean isMarker(Path path) {
            return path.getFileName().toString().endsWith(".acf");
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getAircraftManager();
        }

        @Override
        public Inspection<InstallableArchive> additionalChecks() {
            return Inspections.of(
                    new CheckHasSingleRootFolder()
            );
        }
    },

    SCENERY {
        @Override
        boolean isMarker(Path path) {
            return path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                    && path.getNameCount() == 2;
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getSceneryManager();
        }

        @Override
        public Inspection<InstallableArchive> additionalChecks() {
            return Inspections.of(
                    new CheckHasSingleRootFolder()
            );
        }
    },

    NAVDATA {
        @Override
        boolean isMarker(Path path) {
            return path.endsWith("earth_nav.dat")
                    || path.endsWith("earth_awy.dat")
                    || path.endsWith("earth_fix.dat");
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getNavDataManager();
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

    public Inspection<InstallableArchive> additionalChecks() {
        return Inspections.empty();
    }

    abstract InstallTarget target(@NonNull XPlane xPlane);

    abstract boolean isMarker(Path path);
}
