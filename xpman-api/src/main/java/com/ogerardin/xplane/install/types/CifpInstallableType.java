package com.ogerardin.xplane.install.types;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.install.InstallableType;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;

import java.io.IOException;
import java.util.Objects;

/**
 * Installable type for FAA CIFP (ARINC 424) navigation data.
 * Recognizes archives containing an entry named exactly FAACIFP18 and installs the
 * converted X-Plane data set plus earth_424.dat into the Custom Data folder.
 */
@SuppressWarnings("unused")
public class CifpInstallableType implements InstallableType {

    /** The name of the extensionless file the FAA ships the current cycle in. */
    private static final String FAACIFP18 = "FAACIFP18";

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
        // Nothing to validate up front: the converter is fetched on demand and the
        // X-Plane destination paths are only written during install.
        return InspectionResult.empty();
    }

    @Override
    public void install(XPlane xPlane, Archive archive, ProgressListener progress) throws InstallationException {
        try {
            xPlane.getCifpManager().install(archive, progress);
        } catch (IOException e) {
            throw new InstallationException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InstallationException("CIFP conversion was interrupted", e);
        }
    }
}