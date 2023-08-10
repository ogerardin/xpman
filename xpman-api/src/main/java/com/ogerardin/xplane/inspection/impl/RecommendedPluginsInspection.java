package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.Plugin;
import lombok.NonNull;

import java.util.Set;

/**
 * This {@link Inspection} will check that the given {@link XPlane} has the recommended plugins installed
 * and produce a warning message for each missing plugin.
 */
public class RecommendedPluginsInspection implements Inspection<XPlane> {

    private final Set<Class<? extends Plugin>> wantedPluginClasses;

    @SafeVarargs
    public RecommendedPluginsInspection(Class<? extends Plugin>... wantedPluginClasses) {
        this.wantedPluginClasses = Set.of(wantedPluginClasses);
    }

    @Override
    public InspectionResult inspect(@NonNull XPlane target) {
        var messages = wantedPluginClasses.stream()
                .filter(pluginClass -> !isPluginInstalled(target, pluginClass))
                .map(pluginClass -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(target.toString())
                        .message("Recommended plugin " + pluginClass.getSimpleName() + " is not installed for this plane")
                        .build()
                )
                .toList();
        return InspectionResult.of(messages);
    }

    private static Boolean isPluginInstalled(XPlane xPlane, Class<? extends Plugin> wantedPluginClass) {
        return xPlane.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }
}
