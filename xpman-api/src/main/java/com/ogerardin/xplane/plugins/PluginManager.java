package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PluginManager extends Manager<Plugin> {

    @NonNull
    @Getter
    private final Path pluginsFolder;

    private  List<Plugin> plugins = null;

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


    @Synchronized
    private void loadPlugins()  {

        fireEvent(new ManagerEvent.Loading<>());

        try (Stream<Path> pathStream = Files.list(pluginsFolder)) {
            plugins = pathStream
                    .filter(Files::isDirectory)
                    .map(this::maybeGetPlugin)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Plugin folder not found: {}", pluginsFolder);
            plugins = Collections.emptyList();
        }

        fireEvent(new ManagerEvent.Loaded<>(plugins));
    }

    private Optional<Plugin> maybeGetPlugin(Path path) {
        try {
            final Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, path);
            return Optional.of(plugin);
        } catch (InstantiationException e) {
            return Optional.empty();
        }
    }

}
