package com.ogerardin.xplane.test.plugins;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.InvalidConfig;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.test.DisabledIfNoXPlaneRootFolder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
class PluginManagerTest {

    @Test
    void testLoadPlugins() throws InvalidConfig {

        XPlane xplane = new XPlane(XPlane.getDefaultXPRootFolder());

        List<Plugin> plugins = xplane.getPluginManager().getPlugins();
        log.info("Found {} plugins", plugins.size());
        plugins.forEach(plugin -> log.info("*** {} version {} ({})", plugin.getName(), plugin.getVersion(), plugin.getClass().getName()));
    }
}
