package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An {@link Inspection} that warns when plugins conflicting with the add-on being installed are
 * present: maps each conflicting plugin's base folder name (case-insensitive) to the reason it
 * conflicts; every installed match yields one non-blocking WARN. Probes installed folder names
 * without building Plugin objects, so it stays cheap enough for the wizard's FX thread.
 */
@RequiredArgsConstructor
public class ConflictingPluginsInspection implements Inspection<Archive> {

    private final XPlane xPlane;
    private final Map<String, String> conflicts;

    @Override
    public InspectionResult inspect(@NonNull Archive archive) {
        if (xPlane == null || conflicts.isEmpty()) {
            return InspectionResult.empty();
        }
        Set<String> installedFolderNames = xPlane.getPluginManager().getInstalledPluginFolderNames();
        List<InspectionMessage> messages = conflicts.entrySet().stream()
                .filter(entry -> installedFolderNames.stream()
                        .anyMatch(name -> entry.getKey().equalsIgnoreCase(name)))
                .map(entry -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(entry.getKey())
                        .message("This plugin conflicts with the add-on being installed")
                        .details(entry.getValue())
                        .build())
                .toList();
        return messages.isEmpty() ? InspectionResult.empty() : InspectionResult.of(messages);
    }
}
