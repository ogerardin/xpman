package com.ogerardin.xplane.install.inspections;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * An {@link Inspection} that warns when plugins conflicting with the add-on being installed are
 * present: maps each conflicting plugin's base folder name (case-insensitive) to the reason it
 * conflicts; every installed match yields one non-blocking WARN.
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
        List<InspectionMessage> messages = xPlane.getPluginManager().getPlugins().stream()
                .filter(plugin -> reasonFor(plugin) != null)
                .map(plugin -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(plugin.getName())
                        .message("This plugin conflicts with the add-on being installed")
                        .details(reasonFor(plugin))
                        .build())
                .toList();
        return messages.isEmpty() ? InspectionResult.empty() : InspectionResult.of(messages);
    }

    private String reasonFor(Plugin plugin) {
        String folder = plugin.getBaseFolder().getFileName().toString();
        return conflicts.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(folder))
                .map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }
}
