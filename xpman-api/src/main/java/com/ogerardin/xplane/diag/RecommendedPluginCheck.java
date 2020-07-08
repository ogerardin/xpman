package com.ogerardin.xplane.diag;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.config.plugins.Plugin;

public class RecommendedPluginCheck extends PredicateCheckItem<Aircraft> {

    public RecommendedPluginCheck(Class<? extends Plugin> wantedPluginClass) {
        super(Severity.INFO, "Plugin " + wantedPluginClass.getSimpleName() + " is recommended for this plane",
                (aircraft, xPlaneInstance) -> pluginInstalled(xPlaneInstance, wantedPluginClass));
    }

    private static Boolean pluginInstalled(XPlaneInstance xPlaneInstance, Class<? extends Plugin> wantedPluginClass) {
        return xPlaneInstance.getPluginManager().getPlugins().stream()
                .map(Plugin::getClass)
                .anyMatch(pluginClass -> pluginClass == wantedPluginClass);
    }

}
