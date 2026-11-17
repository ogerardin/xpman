package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.AssertHasSingleRootFolder;
import com.ogerardin.xplane.install.inspections.WouldOverwriteInspection;
import com.ogerardin.xplane.install.inspections.SkunkcraftsUpdatableInspection;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Installable type for scenery packages.
 * Recognizes archives containing "Earth nav data" folder and installs to Custom Scenery.
 * After unpacking, registers the new folder at the bottom of scenery_packs.ini so it shows up
 * immediately as an enabled, ranked entry instead of waiting for X-Plane to append it on its
 * next launch.
 */
@SuppressWarnings("unused")
public class SceneryInstallableType implements InstallableType {

    @Override
    public String description() {
        return "scenery package";
    }

    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                    && path.getNameCount() == 2);
    }
    
    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return AssertHasSingleRootFolder.INSTANCE
                .and(SkunkcraftsUpdatableInspection.INSTANCE)
                .and(xPlane == null ? Inspection.empty() : new WouldOverwriteInspection(xPlane.getSceneryManager()))
                .and(xPlane == null ? Inspection.empty() : sceneryIniMissing(xPlane))
                .inspectable(archive)
                .inspect();
    }
    
    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getSceneryManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        }
        // Register the new folder at the bottom of scenery_packs.ini so X-Plane loads it.
        Optional<String> rootFolder = rootFolderName(archive);
        if (rootFolder.isPresent()) {
            xPlane.getSceneryManager().addToIniEnd(rootFolder.get());
        }
    }

    private static Optional<String> rootFolderName(Archive archive) {
        return archive.getPaths().stream()
                .filter(path -> path.getNameCount() > 1)
                .map(path -> path.getName(0).toString())
                .distinct()
                .reduce((a, b) -> a); // keeps first (or empty if no multi-name entries)
    }

    private static Inspection<Archive> sceneryIniMissing(XPlane xPlane) {
        Path iniFile = xPlane.getPaths().customScenery().resolve("scenery_packs.ini");
        if (Files.exists(iniFile)) {
            return Inspection.empty();
        }
        return archive -> InspectionResult.of(InspectionMessage.warn(
                "scenery_packs.ini does not exist yet (X-Plane has never launched with this profile). " +
                "The package will be installed but not registered; X-Plane will register it on first launch."));
    }
}
