package com.ogerardin.xplane.test.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.SimLoadManagerPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the SimLoadManagerPlugin custom plugin class.
 */
class SimLoadManagerPluginTest {

    @TempDir
    Path tempDir;

    @Test
    void recognizesSimLoadManagerFolder() throws Exception {
        // Create a fake X-Plane installation with SimLoadManager plugin
        Path pluginsDir = tempDir.resolve("Resources/plugins");
        Files.createDirectories(pluginsDir);
        
        Path simLoadManagerDir = pluginsDir.resolve("SimLoadManager/mac_x64");
        Files.createDirectories(simLoadManagerDir);
        
        Path xplFile = simLoadManagerDir.resolve("SimLoadManager.xpl");
        Files.createFile(xplFile);
        
        XPlane xPlane = new XPlane(tempDir);
        
        SimLoadManagerPlugin plugin = new SimLoadManagerPlugin(xPlane, xplFile);
        
        assertThat(plugin.getName(), is("SimLoad Manager"));
        assertThat(plugin.getDesc(), is("Realistic PAX, cargo & fuel ground operations"));
    }

    @Test
    void rejectsNonSimLoadManagerFolder() throws Exception {
        // Create a fake X-Plane installation with a different plugin
        Path pluginsDir = tempDir.resolve("Resources/plugins");
        Files.createDirectories(pluginsDir);
        
        Path otherPluginDir = pluginsDir.resolve("SomeOtherPlugin/mac_x64");
        Files.createDirectories(otherPluginDir);
        
        Path xplFile = otherPluginDir.resolve("SomeOtherPlugin.xpl");
        Files.createFile(xplFile);
        
        XPlane xPlane = new XPlane(tempDir);
        
        assertThrows(InstantiationException.class, () -> {
            new SimLoadManagerPlugin(xPlane, xplFile);
        });
    }

    @Test
    void acceptsCaseInsensitiveFolderName() throws Exception {
        // Create a fake X-Plane installation with case-insensitive folder name
        Path pluginsDir = tempDir.resolve("Resources/plugins");
        Files.createDirectories(pluginsDir);
        
        Path simLoadManagerDir = pluginsDir.resolve("simloadmanager/mac_x64");
        Files.createDirectories(simLoadManagerDir);
        
        Path xplFile = simLoadManagerDir.resolve("SimLoadManager.xpl");
        Files.createFile(xplFile);
        
        XPlane xPlane = new XPlane(tempDir);
        
        SimLoadManagerPlugin plugin = new SimLoadManagerPlugin(xPlane, xplFile);
        
        assertThat(plugin.getName(), is("SimLoad Manager"));
    }
}
