package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.inspections.CheckInstallTypeMatches;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * An installer that is able to process any type of X-Plane installation archive defined in {@link InstallType}
 */
@RequiredArgsConstructor
public class GenericInstaller {

    @NonNull
    private final XPlane xPlane;

    @NonNull
    private final ArchiveInstallSource installSource;

    /** The installation type to perform or null for automatic based on archive contents */
    private final InstallType installType;


    public GenericInstaller(@NonNull XPlane xPlane, @NonNull ArchiveInstallSource archive) {
        this(xPlane, archive, null);
    }

    public InspectionResult inspect() {
        return installSource
                .and(new CheckInstallTypeMatches(installType).inspectable(installSource))
                .inspect();
    }

    @SneakyThrows
    public void install(ProgressListener progressListener) {
        // assumes that inspect() has been called and returned no error, which means
        // that there is one and only one candidate type
        InstallType installType = installSource.getInstallType().orElseThrow(() -> new IllegalStateException("No install type identified"));
        InstallTarget target = installType.target(xPlane);
        target.install(installSource.getArchive(), progressListener);
    }

}
