package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.install.inspections.custom.NavigraphCycleVersion;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import org.apache.commons.lang.WordUtils;

import java.util.function.Predicate;

/**
 * Represents a type of installation that can be performed with an {@link Archive}.
 */
public enum InstallType implements Predicate<Archive> {

    AIRCRAFT {
        // archive is recognized as an aircraft source if it containes at least one .acf file
        @Override
        public boolean test(Archive archive) {
            return archive.getPaths().stream()
                    .anyMatch(path -> path.getFileName().toString().endsWith(".acf"));
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getAircraftManager();
        }

        @Override
        public Inspection<Archive> additionalInspections() {
            return CheckHasSingleRootFolder.INSTANCE;
        }
    },

    SCENERY {
        // archive is recognized as a scenery source if it contains at least one folder "Earth nav data"
        @Override
        public boolean test(Archive archive) {
            return archive.getPaths().stream()
                    .anyMatch(path -> path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                            && path.getNameCount() == 2);
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getSceneryManager();
        }

        @Override
        public Inspection<Archive> additionalInspections() {
            return CheckHasSingleRootFolder.INSTANCE;
        }
    },

    NAVDATA {
        // archive is recognized as a nav data source if it containes at least one file "earth_xxx.dat"
        @Override
        public boolean test(Archive archive) {
            return archive.getPaths().stream()
                    .anyMatch(path -> path.endsWith("earth_nav.dat")
                            || path.endsWith("earth_awy.dat")
                            || path.endsWith("earth_fix.dat"));
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getNavDataManager();
        }

        @Override
        public Inspection<Archive> additionalInspections() {
            return NavigraphCycleVersion.INSTANCE;
        }
    },

    PLUGIN {
        // archive is recognized as a plugin source if it containes at least one .xpl file
        // FIXME this test is not correct as some aircraft archives also include plugins...
        @Override
        public boolean test(Archive archive) {
            return archive.getPaths().stream()
                    .anyMatch(path -> path.getFileName().toString().endsWith(".xpl"));
        }

        @Override
        InstallTarget target(@NonNull XPlane xPlane) {
            return xPlane.getPluginManager();
        }
    };


    @Override
    public String toString() {
        return WordUtils.capitalizeFully(name());
    }

    public Inspection<Archive> additionalInspections() {
        return Inspection.empty();
    }

    /**
     * Returns the {@link InstallTarget} for this type, e.g. for an aircraft the target will be the
     * {@link com.ogerardin.xplane.aircrafts.AircraftManager}.
     */
    abstract InstallTarget target(@NonNull XPlane xPlane);
}
