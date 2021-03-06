package com.ogerardin.xplane.plugins;

import com.ogerardin.util.DisabledIfNoXPlaneRootFolder;
import com.ogerardin.util.TimingExtension;
import com.ogerardin.xplane.InvalidConfig;
import com.ogerardin.xplane.XPlane;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@Slf4j
@ExtendWith(TimingExtension.class)
@DisabledIfNoXPlaneRootFolder
public class PluginManagerTest {

    @Test
    public void testLoadPlugins() throws InvalidConfig {

        XPlane xplane = new XPlane(XPlane.getDefaultXPRootFolder());

        List<Plugin> plugins = xplane.getPluginManager().getPlugins();
        log.info("Found {} plugins", plugins.size());
        plugins.forEach(plugin -> log.info("*** {} version {} ({})", plugin.getName(), plugin.getVersion(), plugin.getClass().getName()));
    }
}
