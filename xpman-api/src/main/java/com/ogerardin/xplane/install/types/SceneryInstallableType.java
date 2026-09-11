package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;

/**
 * Installable type for scenery packages.
 * Recognizes archives containing "Earth nav data" folder and installs to Custom Scenery.
 */
@SuppressWarnings("unused")
public class SceneryInstallableType implements InstallableType {
    
    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                    && path.getNameCount() == 2);
    }
    
    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return CheckHasSingleRootFolder.INSTANCE.inspectable(archive).inspect();
    }
    
    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getSceneryManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        }
    }
}
