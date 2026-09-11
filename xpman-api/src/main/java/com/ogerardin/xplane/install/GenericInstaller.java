package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * An installer that is able to process any type of X-Plane installation archive
 * defined by {@link InstallableType} implementations.
 */
@RequiredArgsConstructor
public class GenericInstaller {

    @NonNull
    private final XPlane xPlane;

    @NonNull
    private final ArchiveInstallSource installSource;

    public InspectionResult inspect() {
        return installSource.inspect();
    }

    public void install(ProgressListener progressListener) throws InstallationException {
        // assumes that inspect() has been called and returned no error
        installSource.install(xPlane, progressListener);
    }

}
