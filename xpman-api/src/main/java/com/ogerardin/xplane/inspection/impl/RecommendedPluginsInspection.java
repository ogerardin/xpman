package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.plugins.Plugin;

import java.util.Arrays;
import java.util.List;

public class RecommendedPluginsInspection<T> implements Inspection<T> {

    private final Class<? extends Plugin>[] wantedPluginClasses;

    private final XPlane xPlane;

    @SafeVarargs
    public RecommendedPluginsInspection(XPlane xPlane, Class<? extends Plugin>... wantedPluginClasses) {
        this.xPlane = xPlane;
        this.wantedPluginClasses = wantedPluginClasses;
    }

    @Override
    public List<InspectionMessage> inspect(T target) {
        return Arrays.stream(wantedPluginClasses)
                .filter(pluginClass -> ! pluginInstalled(xPlane, pluginClass))
                .map(pluginClass -> InspectionMessage.builder()
                        .severity(Severity.WARN)
                        .object(target.toString())
                        .message("Recommended plugin " + pluginClass.getSimpleName() + " is not installed for this plane")
                        .build()
                )
                .toList();
    }

    private static Boolean pluginInstalled(XPlane xPlane, Class<? extends Plugin> wantedPluginClass) {
        return xPlane.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }
}
