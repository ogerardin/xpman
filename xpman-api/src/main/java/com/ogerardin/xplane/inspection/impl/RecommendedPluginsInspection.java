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

    @SafeVarargs
    public RecommendedPluginsInspection(Class<? extends Plugin>... wantedPluginClasses) {
        this.wantedPluginClasses = wantedPluginClasses;
    }

    @Override
    public List<InspectionMessage> apply(T target, XPlaneInstance xPlaneInstance) {
        return Arrays.stream(wantedPluginClasses)
                .filter(pluginClass -> ! pluginInstalled(xPlaneInstance, pluginClass))
                .map(pluginClass -> new InspectionMessage(Severity.WARN, target.toString(), "Recommended plugin " + pluginClass.getSimpleName() + " is not installed for this plane"))
                .collect(Collectors.toList());
    }

    private static Boolean pluginInstalled(XPlaneInstance xPlaneInstance, Class<? extends Plugin> wantedPluginClass) {
        return xPlaneInstance.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }
}
