package com.ogerardin.xplane.config.install;

import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import lombok.Data;

import java.nio.file.Path;
import java.util.*;

@Data
public class CheckIsInstallableType implements Inspection<InstallableArchive> {

    @Override
    public List<InspectionMessage> inspect(InstallableArchive zip) {

        Set<InstallType> candidateTypes = InstallType.candidateTypes(zip);

        if (candidateTypes.isEmpty()) {
            return Collections.singletonList(InspectionMessage.builder()
                    .severity(Severity.ERROR).message("Archive type could not be identified. " +
                            "Either file is not an installable X-Plane add-on, or it is corrupt.")
                    .abort(true)
                    .build()
            );
        }

        if (candidateTypes.size() > 1) {
            return Collections.singletonList(InspectionMessage.builder()
                    .severity(Severity.ERROR).message("Mixed content found: " + candidateTypes + ". " +
                            "File can't be installed automatically, check its documentation.")
                    .abort(true)
                    .build()
            );
        }

        InstallType installType = candidateTypes.iterator().next();
        return Collections.singletonList(InspectionMessage.builder()
                        .severity(Severity.INFO).message("Archive type identified as: " + installType)
                        .build()
        );

    }

    private Optional<InstallType> getInstallTypeMarker(Path path) {
        if (path.getFileName().toString().endsWith(".acf")) {
            return Optional.of(InstallType.AIRCRAFT);
        }
        if (path.getFileName().toString().equals(SceneryPackage.EARTH_NAV_DATA)
                && path.getNameCount() == 2) {
            return Optional.of(InstallType.SCENERY);
        }
        return Optional.empty();
    }

}
