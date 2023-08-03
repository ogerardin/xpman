package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.install.inspections.CheckInstallType;
import com.ogerardin.xplane.install.inspections.CheckIsValidArchive;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * An installer that is able to process any type of X-Plane installation archive defined in {@link InstallType}
 */
public class GenericInstaller {

    @NonNull
    private final XPlane xPlane;

    @NonNull
    private final InstallableArchive installableArchive;

    /** The install type to perform or null for automatic based on archive contents */
    private final InstallType installType;

    @Getter(lazy = true)
    private final Set<InstallType> candidateTypes = computeCandidateTypes();

    private Set<InstallType> computeCandidateTypes() {
        return InstallType.candidateTypes(installableArchive);
    }

    public GenericInstaller(@NonNull XPlane xPlane, @NonNull Path archive) {
        this(xPlane, archive, null);
    }

    public GenericInstaller(@NonNull XPlane xPlane, @NonNull Path archive, InstallType installType) {
        this.xPlane = xPlane;
        this.installableArchive = new InstallableZip(archive);
        this.installType = installType;
    }

    public List<InspectionMessage> inspect() {
        Inspections<InstallableArchive> archiveInspections = Inspections.of(
                new CheckIsValidArchive(),
                new CheckInstallType(installType)
        );
        return archiveInspections.inspect(installableArchive);
    }

    @SneakyThrows
    public void install(ProgressListener progressListener) {
        // assumes that inspect() has been called and returned no error, which means
        // that there is one and only one candidate type
        InstallType installType = getCandidateTypes().iterator().next();
        InstallTarget target = installType.target(xPlane);
        target.install(installableArchive, progressListener);
    }

}
