package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;

/**
 * Installable type for plugins.
 * Recognizes archives containing .xpl files and installs to the plugins folder.
 */
public class PluginInstallableType implements InstallableType {
    
    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
            .anyMatch(path -> path.getFileName().toString().endsWith(".xpl"));
    }
    
    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return CheckHasSingleRootFolder.INSTANCE.inspectable(archive).inspect();
    }
    
    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getPluginManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        }
    }
}
