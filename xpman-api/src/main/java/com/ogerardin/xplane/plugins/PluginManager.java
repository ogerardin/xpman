package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
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
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class PluginManager extends Manager<Plugin> {

    @NonNull
    @Getter
    private final Path pluginsFolder;

    private static final Map<String, Class<? extends Platform>> PLATFORM_PLUGIN_MAP = Maps.mapOf(
            "mac.xpl", MacPlatform.class,
            "win.xpl", WindowsPlatform.class,
            "lin.xpl", LinuxPlatform.class
    );



    public PluginManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.pluginsFolder = xPlane.getPaths().plugins();
    }

    public List<Plugin> getPlugins() {
        if (items == null) {
            loadPlugins();
        }
        return Collections.unmodifiableList(items);
    }


    /**
     * Trigger an asynchronous reload of the aircraft list.
     */
    public void reload() {
        AsyncHelper.runAsync(this::loadPlugins);
    }


    @SneakyThrows
//    @Synchronized
    private void loadPlugins()  {

        log.info("Loading plugins...");
        fireEvent(ManagerEvent.<Plugin>builder().type(LOADING).source(this).build());

        items = FileUtils.findFiles(pluginsFolder, path -> path.getFileName().toString().endsWith(".xpl")).stream()
                .filter(this::isNotUnwantedPlatform)
                .map(this::maybeGetPlugin)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        log.info("Loaded {} plugins", items.size());
        fireEvent(ManagerEvent.<Plugin>builder().type(LOADED).source(this).items(items).build());
    }

    private boolean isNotUnwantedPlatform(Path xplFile) {
        String filename = xplFile.getFileName().toString();
        Class<? extends Platform> pluginPlatform = PLATFORM_PLUGIN_MAP.get(filename);
        return (pluginPlatform == null) || (pluginPlatform == MacPlatform.class);
    }

    private Optional<Plugin> maybeGetPlugin(Path xplFile) {
        try {
            final Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xPlane, xplFile);
            return Optional.of(plugin);
        } catch (InstantiationException e) {
            return Optional.empty();
        }
    }

}
