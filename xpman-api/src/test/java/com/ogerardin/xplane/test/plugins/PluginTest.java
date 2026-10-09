package com.ogerardin.xplane.test.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class PluginTest {

    @TempDir
    Path tempDir;

    private XPlane newXPlane() throws Exception {
        return new XPlane(tempDir);
    }

    private Path createPlugin(String folderName, String xplFileName) throws IOException {
        Path folder = tempDir.resolve("Resources/plugins/" + folderName);
        Files.createDirectories(folder);
        Path xplFile = folder.resolve(xplFileName);
        Files.write(xplFile, new byte[0]);
        return xplFile;
    }

    @Test
    void pluginInPluginsFolderIsEnabled() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Plugin plugin = new Plugin(newXPlane(), xplFile);

        assertThat(plugin.isEnabled(), is(true));
    }

    @Test
    void disableMovesFolderToDisabledPlugins() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Plugin plugin = new Plugin(newXPlane(), xplFile);

        plugin.setEnabled(false);

        Path disabledXpl = tempDir.resolve("Resources/plugins (disabled)/Foo/Foo.xpl");
        assertThat(Files.exists(disabledXpl), is(true));
        assertThat(Files.exists(xplFile), is(false));

        // the same object stays truthful after the move (enabled state, reveal, uninstall act on the new path)
        assertThat(plugin.isEnabled(), is(false));
        assertThat(plugin.getXplFile(), is(disabledXpl));
        assertThat(plugin.getBaseFolder(), is(tempDir.resolve("Resources/plugins (disabled)/Foo")));

        Plugin reloaded = new Plugin(newXPlane(), disabledXpl);
        assertThat(reloaded.isEnabled(), is(false));
    }

    @Test
    void enableMovesFolderBackToPlugins() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Plugin plugin = new Plugin(newXPlane(), xplFile);
        plugin.setEnabled(false);

        Path disabledXpl = tempDir.resolve("Resources/plugins (disabled)/Foo/Foo.xpl");
        Plugin disabled = new Plugin(newXPlane(), disabledXpl);
        disabled.setEnabled(true);

        assertThat(Files.exists(xplFile), is(true));
        assertThat(Files.exists(disabledXpl), is(false));

        Plugin reloaded = new Plugin(newXPlane(), xplFile);
        assertThat(reloaded.isEnabled(), is(true));
    }

    @Test
    void setEnabledIsIdempotent() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Plugin plugin = new Plugin(newXPlane(), xplFile);

        plugin.setEnabled(true);
        assertThat(Files.exists(xplFile), is(true));
    }

    @Test
    void disabledPluginIsNotSkunkcraftsUpdatable() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Files.writeString(xplFile.getParent().resolve("skunkcrafts_updater.cfg"), "module|https://example.com/\n");
        Plugin plugin = new Plugin(newXPlane(), xplFile);

        assertThat(plugin.isSkunkcraftsUpdatable(), is(true));
        plugin.setEnabled(false);
        assertThat(plugin.isSkunkcraftsUpdatable(), is(false));
    }

    @Test
    void disabledPluginInspectionOmitsUpdateWarning() throws Exception {
        Path xplFile = createPlugin("Foo", "Foo.xpl");
        Files.writeString(xplFile.getParent().resolve("skunkcrafts_updater.cfg"), "module|https://example.com/\n");
        Plugin plugin = new Plugin(newXPlane(), xplFile) {
            @Override public String getLatestVersion() { return "2.0"; }
        };
        plugin.setEnabled(false);

        assertThat(plugin.inspect().isEmpty(), is(true));
    }

    @Test
    void pluginManagerListsDisabledPlugins() throws Exception {
        createPlugin("Enabled", "Enabled.xpl");
        Path disabledFolder = tempDir.resolve("Resources/plugins (disabled)/Disabled");
        Files.createDirectories(disabledFolder);
        Files.write(disabledFolder.resolve("Disabled.xpl"), new byte[0]);

        XPlane xPlane = newXPlane();
        List<Plugin> plugins = xPlane.getPluginManager().getPlugins();

        assertThat(plugins, hasSize(2));
        assertThat(plugins.stream().filter(Plugin::isEnabled).count(), is(1L));
        assertThat(plugins.stream().filter(p -> !p.isEnabled()).count(), is(1L));
    }
}
