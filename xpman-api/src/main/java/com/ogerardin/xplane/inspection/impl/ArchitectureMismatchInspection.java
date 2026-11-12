package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.platform.Architecture;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.List;

/**
 * Warns when an enabled plugin's Mach-O binary contains only x86_64 on an Apple Silicon Mac:
 * X-Plane will not load it unless started under Rosetta. Permissive on doubt: silent when the
 * binary's format is unrecognized or the host is not Apple Silicon.
 */
public enum ArchitectureMismatchInspection implements Inspection<Plugin> {
    INSTANCE;

    @Override
    public @NonNull InspectionResult inspect(@NonNull Plugin target) {
        return inspect(target, Platforms.getCurrent());
    }

    /** Test seam — same logic but with an injected platform. */
    public InspectionResult inspect(@NonNull Plugin target, @NonNull Platform platform) {
        if (!platform.isAppleSilicon()) return InspectionResult.empty();
        Path xpl = target.getXplFile();
        List<Architecture> arches = platform.getBinaryArchitectures(xpl);
        if (arches.size() != 1 || arches.getFirst() != Architecture.X86_64) return InspectionResult.empty();
        return InspectionResult.of(message(target, xpl));
    }

    private static InspectionMessage message(Plugin plugin, Path xpl) {
        return InspectionMessage.builder()
                .severity(Severity.WARN)
                .object(plugin.getName())
                .message(plugin.getName() + ": plugin binary is for Intel Macs — X-Plane will not load it "
                        + "unless started with Rosetta; reinstall with a universal or Apple Silicon build")
                .details(xpl.toString())
                .build();
    }
}
