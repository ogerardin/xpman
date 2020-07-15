package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.config.plugins.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendedPluginsInspection<T> implements Inspection<T> {

    private final Class<? extends Plugin>[] wantedPluginClasses;

    @SafeVarargs
    public RecommendedPluginsInspection(Class<? extends Plugin>... wantedPluginClasses) {
        this.wantedPluginClasses = wantedPluginClasses;
    }

    private static Boolean pluginInstalled(XPlaneInstance xPlaneInstance, Class<? extends Plugin> wantedPluginClass) {
        return xPlaneInstance.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }

    @Override
    public List<InspectionResult> inspect(T target, XPlaneInstance xPlaneInstance) {
        return Arrays.stream(wantedPluginClasses)
                .map(wantedPluginClass -> new InspectionResult(
                        pluginInstalled(xPlaneInstance, wantedPluginClass) ? Severity.OK : Severity.WARN,
                        target.toString(), "Plugin " + wantedPluginClass.getSimpleName() + " is recommended for this plane"
                ))
                .collect(Collectors.toList());
    }
}
