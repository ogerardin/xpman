package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.custom.NavigraphCycleVersion;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;

/**
 * Installable type for navigation data.
 * Recognizes archives containing earth_nav.dat, earth_awy.dat, or earth_fix.dat
 * and installs to Custom Data folder.
 */
@SuppressWarnings("unused")
public class NavDataInstallableType implements InstallableType {
    
    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
            .anyMatch(path -> path.endsWith("earth_nav.dat")
                    || path.endsWith("earth_awy.dat")
                    || path.endsWith("earth_fix.dat"));
    }
    
    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return NavigraphCycleVersion.INSTANCE.inspectable(archive).inspect();
    }
    
    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getNavDataManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        }
    }
}
