package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Installable type for the free FAA CIFP cycle archives (ARINC 424).
 * Installs the FAACIFP18 file as {@code Custom Data/earth_424.dat}, which makes
 * X-Plane load that file as a sim-wide override in place of its global navdata
 * layer.
 */
@SuppressWarnings("unused")
public class CifpInstallableType implements InstallableType {

    /** The name of the extensionless file the FAA ships the current cycle in. */
    private static final String FAACIFP18 = "FAACIFP18";

    /** The name X-Plane looks for in Custom Data to apply a sim-wide ARINC 424 override. */
    private static final String EARTH_424 = "earth_424.dat";

    @Override
    public String description() {
        return "FAA CIFP (ARINC 424)";
    }

    @Override
    public boolean recognizes(Archive archive) {
        return archive.getPaths().stream()
                .anyMatch(path -> Objects.equals(path.getFileName().toString(), FAACIFP18));
    }

    @Override
    public InspectionResult preconditions(XPlane xPlane, Archive archive) {
        return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.WARN)
                .message("Installing this replaces X-Plane's global navigation data with the FAA dataset, and X-Plane "
                        + "then loads no other navdata file, so anything else in Custom Data becomes unused. "
                        + "Only US navdata will be available, and no enroute navaids or airways are loaded: "
                        + "they cannot be replaced safely without breaking the referential integrity of the "
                        + "airway network.")
                .build());
    }

    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        Path entry = archive.getPaths().stream()
                .filter(path -> FAACIFP18.equals(path.getFileName().toString()))
                .findFirst()
                .orElseThrow(() -> new InstallationException("FAACIFP18 not found in the archive"));

        // Extract to a temporary folder rather than straight into Custom Data: the entry
        // may be nested, and this way nothing but the target file is ever left behind.
        Path workingDir = null;
        try {
            workingDir = Files.createTempDirectory("xpman-cifp");
            archive.extract(workingDir, entry::equals, progress);

            Path target = xPlane.getPaths().customData().resolve(EARTH_424);
            progress.output("Installing " + EARTH_424);
            Files.createDirectories(target.getParent());
            Files.copy(workingDir.resolve(entry), target, StandardCopyOption.REPLACE_EXISTING);

            xPlane.getNavDataManager().reload();
        } catch (IOException e) {
            throw new InstallationException(e);
        } finally {
            if (workingDir != null) {
                FileUtils.deleteQuietly(workingDir.toFile());
            }
        }
    }
}