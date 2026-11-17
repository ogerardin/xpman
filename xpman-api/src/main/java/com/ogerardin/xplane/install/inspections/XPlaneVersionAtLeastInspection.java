package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.Versions;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;

/**
 * An {@link Inspection} that warns if the X-Plane installation is older than a minimum version requirement.
 * <p>
 * This inspection compares the installed X-Plane version against a required minimum version
 * and produces a {@link com.ogerardin.xplane.inspection.Severity#WARN WARN} message if
 * the installed version is older than required. The installation can still proceed, but
 * the add-on may not work correctly until X-Plane is updated.
 * <p>
 * Follows the house philosophy: if the X-Plane version cannot be determined (null), the
 * inspection is skipped rather than blocking the installation.
 */
@RequiredArgsConstructor
public class XPlaneVersionAtLeastInspection implements Inspection<Archive> {

    private final XPlane xPlane;
    private final String minimumVersion;

    @Override
    public InspectionResult inspect(Archive archive) {
        // House philosophy: missing probe skips the warning; same when XPlane is not in context.
        if (xPlane == null) {
            return InspectionResult.empty();
        }
        String installedVersion = xPlane.getVersion();
        if (installedVersion == null) {
            return InspectionResult.empty();
        }
        
        // Compare versions: if installed >= minimum, pass
        if (Versions.compare(installedVersion, minimumVersion) >= 0) {
            return InspectionResult.empty();
        }
        
        // Installed version is too old - warn but allow install
        return InspectionResult.of(
            InspectionMessage.builder()
                .severity(com.ogerardin.xplane.inspection.Severity.WARN)
                .object("X-Plane " + installedVersion)
                .message("This add-on requires X-Plane " + minimumVersion + " or later and will probably not work until X-Plane is updated")
                .details("The installed X-Plane is " + installedVersion + ".")
                .build()
        );
    }
}
