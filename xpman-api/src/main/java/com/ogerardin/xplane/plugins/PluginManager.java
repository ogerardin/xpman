package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class PluginManager extends Manager<Plugin> {

    @NonNull
    private final Path pluginsFolder;

    @Getter(lazy = true)
    private final List<Plugin> plugins = loadPlugins();

    public PluginManager(@NonNull XPlane xPlane, @NonNull Path pluginsFolder) {
        super(xPlane);
        this.pluginsFolder = pluginsFolder;
    }

    private List<Plugin> loadPlugins()  {
        try (Stream<Path> pathStream = Files.list(pluginsFolder)) {
            return pathStream
                    .filter(path -> Files.isDirectory(path))
                    .map(this::maybeGetPlugin)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Plugin folder not found: {}", pluginsFolder);
            return Collections.emptyList();
        }
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
