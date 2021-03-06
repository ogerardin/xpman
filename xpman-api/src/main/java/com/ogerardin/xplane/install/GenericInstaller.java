package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.inspections.CheckHasSingleRootFolder;
import com.ogerardin.xplane.install.inspections.CheckIsInstallableType;
import com.ogerardin.xplane.install.inspections.CheckIsValidArchive;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import lombok.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * An {@link Installer} that is able to process any type of X-Plane installation archive.
 */
public class GenericInstaller {

    @NonNull
    private final XPlane xPlane;

    @NonNull
    private final InstallableArchive installableArchive;

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
        return inspectArchive();
    }

    private List<InspectionMessage> inspectArchive() {
        Inspections<InstallableArchive> archiveInspections = Inspections.of(
                new CheckIsValidArchive(),
                new CheckIsInstallableType(installType),
                new CheckHasSingleRootFolder()
        );
        return archiveInspections.inspect(installableArchive);
    }

    @SneakyThrows
    public void install(Installer.ProgressListener progressListener) {
        InstallType installType = getCandidateTypes().iterator().next();
        InstallTarget target = installType.target(xPlane);
        target.install(installableArchive, progressListener);
    }

}
