package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.util.Versions;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.RequiredArgsConstructor;

/**
 * An {@link Inspection} that checks if the X-Plane installation meets a minimum version requirement.
 * <p>
 * This inspection compares the installed X-Plane version against a required minimum version
 * and produces an {@link com.ogerardin.xplane.inspection.Severity#ERROR ERROR} message if
 * the installed version is older than required.
 * <p>
 * Follows the house philosophy: if the X-Plane version cannot be determined (null), the
 * inspection is skipped rather than blocking the installation.
 */
@RequiredArgsConstructor
public class AssertXPlaneVersionAtLeast implements Inspection<Archive> {

    private final XPlane xPlane;
    private final String minimumVersion;

    @Override
    public InspectionResult inspect(Archive archive) {
        String installedVersion = xPlane.getVersion();
        
        // House philosophy: missing probe skips the warning
        if (installedVersion == null) {
            return InspectionResult.empty();
        }
        
        // Compare versions: if installed >= minimum, pass
        if (Versions.compare(installedVersion, minimumVersion) >= 0) {
            return InspectionResult.empty();
        }
        
        // Installed version is too old - block installation
        return InspectionResult.of(
            InspectionMessage.builder()
                .severity(com.ogerardin.xplane.inspection.Severity.ERROR)
                .object("X-Plane " + installedVersion)
                .message("X-Plane version too old")
                .details("This add-on requires X-Plane " + minimumVersion + " or later, " +
                         "but the installation is version " + installedVersion + ".")
                .abort(true)
                .build()
        );
    }
}
