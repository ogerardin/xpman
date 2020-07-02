package com.ogerardin.xplane.config.plugins;

import com.ogerardin.xplane.config.plugins.custom.CustomPlugin;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Slf4j
public class PluginManager {

    @NonNull
    public final Path pluginsFolder;

    @Getter(lazy = true)
    private final List<Plugin> plugins = loadPlugins();

    @Getter(AccessLevel.NONE)
    private final List<Class<?>> pluginClasses = findPluginClasses();

    private List<Plugin> loadPlugins()  {
        try (Stream<Path> pathStream = Files.list(pluginsFolder)) {
            return pathStream
                    .filter(path -> Files.isDirectory(path))
                    .map(this::maybeGetPlugin)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Plugin folder not found: {}", pluginsFolder);
            return Collections.emptyList();
        }
    }

    private Optional<Plugin> maybeGetPlugin(Path path) {
        for (Class<?> pluginClass : pluginClasses) {
            try {
                Constructor<?> constructor = pluginClass.getConstructor(Path.class);
                Plugin plugin = (Plugin) constructor.newInstance(path);
                log.info("Found known custom plugin {} in {}", pluginClass.getSimpleName(), path);
                return Optional.of(plugin);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
//                log.debug("Failed to instantiate {} for {}: {}", aircraftClass, acfFile, e.toString());
            }
        }

        return Optional.of(new Plugin(path, path.getFileName().toString()));
    }

    private List<Class<?>> findPluginClasses() {
        String customPluginsPackageName = CustomPlugin.class.getPackage().getName();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .whitelistPackages(customPluginsPackageName)
                .scan()
        ) {
            ClassInfoList aircraftClasses = scanResult.getSubclasses(Plugin.class.getName());
            List<Class<?>> classes = aircraftClasses.loadClasses();
            log.info("Custom plugin classes found: {}", classes);
            return classes;
        }
    }
}
