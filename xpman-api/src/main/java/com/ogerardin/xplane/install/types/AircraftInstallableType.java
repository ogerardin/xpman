package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.AssertHasSingleRootFolder;
import com.ogerardin.xplane.install.inspections.WouldOverwriteInspection;
import com.ogerardin.xplane.install.inspections.SkunkcraftsUpdatableInspection;
import com.ogerardin.xplane.install.inspections.AircraftSpecVersionInspection;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Installable type for aircraft.
 * Recognizes archives containing .acf files and installs them to the aircraft folder.
 */
@SuppressWarnings("unused")
public class AircraftInstallableType implements InstallableType {

    @Override
    public String description() {
        return "generic aircraft";
    }

    @Override
    public boolean recognizes(Archive archive) {
        return isAircraftArchive(archive);
    }

    /**
     * An archive is an aircraft archive if it contains .acf files AND every .xpl is nested 
     * under a "plugins/" directory (aircraft-local plugin convention).
     */
    static boolean isAircraftArchive(Archive archive) {
        boolean hasAcf = archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().endsWith(".acf"));
        
        if (!hasAcf) {
            return false;
        }
        
        // Check if all .xpl files are under a "plugins/" directory
        return archive.getPaths().stream()
            .filter(path -> path.getFileName().toString().endsWith(".xpl"))
            .allMatch(AircraftInstallableType::isAircraftLocalPlugin);
    }

    /**
     * Returns true if the .xpl is under a "plugins/" path element (aircraft-local plugin).
     */
    private static boolean isAircraftLocalPlugin(Path path) {
        for (Path element : path) {
            if (element.toString().equalsIgnoreCase("plugins")) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return AssertHasSingleRootFolder.INSTANCE
                .and(SkunkcraftsUpdatableInspection.INSTANCE)
                .and(xPlane == null ? Inspection.empty() : new WouldOverwriteInspection(xPlane.getAircraftManager()))
                .and(xPlane == null ? Inspection.empty() : new AircraftSpecVersionInspection(xPlane))
                .inspectable(archive)
                .inspect();
    }
    
    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getAircraftManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        }
    }
}
