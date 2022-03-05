package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.platform.LinuxPlatform;
import com.ogerardin.xplane.util.platform.MacPlatform;
import com.ogerardin.xplane.util.platform.Platform;
import com.ogerardin.xplane.util.platform.WindowsPlatform;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PluginManager extends Manager<Plugin> {

    @NonNull
    @Getter
    private final Path pluginsFolder;

    private  List<Plugin> plugins = null;

    private static final Map<String, Class<? extends Platform>> PLAFORM_PLUGIN_MAP = Maps.mapOf(
            "mac.xpl", MacPlatform.class,
            "win.xpl", WindowsPlatform.class,
            "lin.xpl", LinuxPlatform.class
    );



    public PluginManager(@NonNull XPlane xPlane, @NonNull Path pluginsFolder) {
        super(xPlane);
        this.pluginsFolder = pluginsFolder;
    }

    public List<Plugin> getPlugins() {
        if (plugins == null) {
            loadPlugins();
        }
        return Collections.unmodifiableList(plugins);
    }


    /**
     * Trigger an asynchronous reload of the aircraft list.
     */
    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadPlugins);
    }


    @SneakyThrows
    @Synchronized
    private void loadPlugins()  {

        log.info("Loading plugins...");
        fireEvent(new ManagerEvent.Loading<>());

        this.plugins = FileUtils.findFiles(pluginsFolder, path -> path.getFileName().toString().endsWith(".xpl")).stream()
                .filter(this::isNotUnwantedPlatform)
                .map(this::maybeGetPlugin)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("Loaded {} plugins", this.plugins.size());
        fireEvent(new ManagerEvent.Loaded<>(this.plugins));
    }

    private boolean isNotUnwantedPlatform(Path xplFile) {
        String filename = xplFile.getFileName().toString();
        Class<? extends Platform> pluginPlatform = PLAFORM_PLUGIN_MAP.get(filename);
        return (pluginPlatform == null) || (pluginPlatform == MacPlatform.class);
    }

    private Optional<Plugin> maybeGetPlugin(Path xplFile) {
        try {
            final Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xplFile);
            return Optional.of(plugin);
        } catch (InstantiationException e) {
            return Optional.empty();
        }
    }

}
