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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Installable type for the free FAA CIFP cycle archives (ARINC 424).
 *
 * <p>Installs the {@code FAACIFP18} file as {@code Custom Data/FAACIFP18}, which X-Plane
 * reads as its "updated approaches" layer: it overrides approach, airport and terminal data
 * for US airports on top of whatever global navdata is installed, leaving the rest of the
 * world alone.</p>
 *
 * <p>Deliberately not {@code Custom Data/earth_424.dat}. That is X-Plane's sim-wide ARINC 424
 * override, documented for providers holding a <em>global</em> 424 master; after reading it
 * X-Plane loads no other navdata file at all. The FAA publishes US-only cycles, so installing
 * it there would blank out navdata for the whole rest of the world.</p>
 */
@SuppressWarnings("unused")
public class CifpInstallableType implements InstallableType {

    /** The name of the extensionless file the FAA ships the current cycle in, and the name
     *  X-Plane expects to find it under in Custom Data. */
    private static final String FAACIFP18 = "FAACIFP18";

    /** X-Plane's sim-wide ARINC 424 override file, which suppresses every other navdata layer. */
    private static final String SIM_WIDE_OVERRIDE_FILE = "earth_424.dat";

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
        List<InspectionMessage> messages = new ArrayList<>();
        messages.add(InspectionMessage.builder()
                .severity(Severity.WARN)
                .message("Installs the FAA's approach data — approaches, terminal fixes and navaids, "
                        + "ILS, GLS, airport and runway records — as Custom Data/" + FAACIFP18 + ". "
                        + "Only US airports are affected; everything outside the US keeps coming from your "
                        + "existing navdata. X-Plane applies this file only if its AIRAC cycle matches your "
                        + "navdata, because terminal procedures reference waypoints outside the terminal area.")
                .build());

        // Probed directly rather than through NavDataManager: asking the manager here would
        // force a full navdata layer scan, and fire reload events, just to check one file.
        // The sim-wide override layer holds no other file than this one.
        Path override = xPlane.getPaths().customData().resolve(SIM_WIDE_OVERRIDE_FILE);
        if (Files.exists(override)) {
            messages.add(InspectionMessage.builder()
                    .severity(Severity.WARN)
                    .message("Custom Data/" + SIM_WIDE_OVERRIDE_FILE + " is installed. X-Plane reads no "
                            + "other navdata file while it is there, so this will have no effect until it "
                            + "is removed or renamed.")
                    .build());
        }
        return InspectionResult.of(messages);
    }

    @Override
    @SuppressWarnings("java:S5443") // Short-lived staging directory under the OS temp root; deleted in finally.
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        Path entry = archive.getPaths().stream()
                .filter(path -> FAACIFP18.equals(path.getFileName().toString()))
                .findFirst()
                .orElseThrow(() -> new InstallationException("FAACIFP18 not found in the archive"));

        // Extract to a temporary folder rather than straight into Custom Data: the entry may be
        // nested, and the FAA archive also carries readme PDFs and a spreadsheet that must not land
        // there. This way nothing but the target file is ever left behind.
        Path workingDir = null;
        try {
            workingDir = Files.createTempDirectory("xpman-cifp");
            archive.extract(workingDir, entry::equals, progress);

            Path target = xPlane.getPaths().customData().resolve(FAACIFP18);
            progress.output("Installing " + FAACIFP18);
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
