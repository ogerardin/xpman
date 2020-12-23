package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.var;

import java.nio.file.Path;
import java.util.List;

/**
 * An {@link Installer} that is able to process any type of X-Plane installation archive.
 */
@RequiredArgsConstructor
public class GenericInstaller implements Installer {

    private final XPlaneInstance xPlane;

    public List<InspectionMessage> inspect(Path zipFile) {
        Inspections<InstallableArchive> inspections = getInspections();
        var installableZip = new InstallableArchive(zipFile);
        return inspections.inspect(installableZip);
    }

    protected Inspections<InstallableArchive> getInspections() {
        return Inspections.of(
                new CheckIsValidZip(),
                new CheckHasSingleRootFolder(),
                new CheckIsInstallableType()
        );
    }

    @SneakyThrows
    public void install(Path zipFile) {
        var installableZip = new InstallableArchive(zipFile);
        //FIXME don't call candidateTypes again... pass ZIP file in constructor?
        InstallType installType = InstallType.candidateTypes(installableZip).iterator().next();

        Path targetFolder = targetFolder(installType);
        installableZip.installTo(targetFolder);
    }

    private Path targetFolder(InstallType installType) {
        switch (installType) {
            case AIRCRAFT:
                return xPlane.getAircraftManager().getAircraftFolder();
            case SCENERY:
                return xPlane.getSceneryManager().getSceneryFolder();
        }
        throw new RuntimeException("unsupported install type " + installType);
    }

}
