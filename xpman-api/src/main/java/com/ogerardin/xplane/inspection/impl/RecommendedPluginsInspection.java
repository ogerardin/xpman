package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.plugins.Plugin;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendedPluginsInspection<T> implements Inspection<T> {

    private final Class<? extends Plugin>[] wantedPluginClasses;

    private final XPlaneInstance xPlaneInstance;

    @SafeVarargs
    public RecommendedPluginsInspection(XPlaneInstance xPlaneInstance, Class<? extends Plugin>... wantedPluginClasses) {
        this.xPlaneInstance = xPlaneInstance;
        this.wantedPluginClasses = wantedPluginClasses;
    }

    @Override
    public List<InspectionMessage> apply(T target) {
        return Arrays.stream(wantedPluginClasses)
                .filter(pluginClass -> ! pluginInstalled(xPlaneInstance, pluginClass))
                .map(pluginClass -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(target.toString())
                        .message("Recommended plugin " + pluginClass.getSimpleName() + " is not installed for this plane")
                        .build()
                )
                .collect(Collectors.toList());
    }

    private static Boolean pluginInstalled(XPlaneInstance xPlaneInstance, Class<? extends Plugin> wantedPluginClass) {
        return xPlaneInstance.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }
}
