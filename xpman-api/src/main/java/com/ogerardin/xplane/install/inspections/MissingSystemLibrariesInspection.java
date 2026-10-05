package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Warns about system libraries that an add-on needs but that are not installed on the current platform.
 * Non-blocking: emits warnings only and never sets the abort flag.
 */
@RequiredArgsConstructor
public class MissingSystemLibrariesInspection implements Inspection<Archive> {

    private final List<SystemLibraryRequirement> requirements;

    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        var current = Platforms.getCurrent();
        return InspectionResult.of(requirements.stream()
                .filter(requirement -> requirement.platform() == current)
                .filter(requirement -> !current.isSharedLibraryPresent(requirement.soname()))
                .map(requirement -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(requirement.soname())
                        .message("Missing system library: " + requirement.soname())
                        .details(requirement.installHint())
                        .build())
                .toList());
    }

    /**
     * A shared library that must be installed on the system for an add-on to run.
     * @param platform the platform the requirement applies to
     * @param soname the library name prefix to look for, e.g. {@code libopenal.so}
     * @param installHint how to install the library on that platform
     */
    public record SystemLibraryRequirement(Platforms platform, String soname, String installHint) {}
}
