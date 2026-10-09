package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallTarget;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.FileUtils;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import com.ogerardin.xplane.util.zip.Archive;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class PluginManager extends Manager<Plugin> implements InstallTarget {

    /**
     * Represents the root folder of a plugin in an archive.
     * 
     * @param path the path to the plugin root folder within the archive (may be empty for archive root)
     * @param name the name to use for the plugin folder when extracting
     */
    private record PluginRoot(Path path, String name) {}

    @NonNull
    @Getter
    private final Path pluginsFolder;

    @Getter
    private final Path disabledPluginsFolder;

    public PluginManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.pluginsFolder = xPlane.getPaths().plugins();
        this.disabledPluginsFolder = xPlane.getPaths().disabledPlugins();
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
    @Synchronized
    private void loadPlugins()  {

        log.info("Loading plugins...");
        fireEvent(ManagerEvent.<Plugin>builder().type(LOADING).source(this).build());

        List<Path> xplFiles = new ArrayList<>();
        for (Path folder : List.of(pluginsFolder, disabledPluginsFolder)) {
            xplFiles.addAll(FileUtils.findFiles(folder, path -> path.getFileName().toString().endsWith(".xpl")));
        }
        log.debug("Found {} .xpl files: {}", xplFiles.size(), xplFiles);
        
        items = xplFiles.stream()
                .filter(Platforms.getCurrent()::isMatchingPluginPath)
                .map(xplFile -> {
                    log.debug("Creating plugin for xplFile: {}", xplFile);
                    return maybeGetPlugin(xplFile);
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        items.forEach(p -> {
            p.getVersion();
            p.getLatestVersion();
            // pre-warm FlyWithLua scripts here (async thread) so that the UI rebuild doesn't
            // pay the lazy lua header parsing cost on the FX thread
            if (p instanceof FlyWithLua fwl) fwl.getScripts();
        });

        log.info("Loaded {} plugins: {}", items.size(), items.stream().map(p -> p.getName() + " (" + p.getXplFile() + ")").toList());
        fireEvent(ManagerEvent.<Plugin>builder().type(LOADED).source(this).items(items).build());
    }

    private Optional<Plugin> maybeGetPlugin(Path xplFile) {
        try {
            final Plugin plugin = IntrospectionHelper.getBestSubclassInstance(Plugin.class, xPlane, xplFile);
            return Optional.of(plugin);
        } catch (InstantiationException _) {
            return Optional.empty();
        }
    }

    @Override
    public void unpack(Archive archive, ProgressListener progressListener) throws IOException {
        PluginRoot pluginRoot = findPluginRoot(archive)
                .orElseThrow(() -> new IOException("Invalid plugin archive: could not determine plugin root folder"));

        Path targetFolder = pluginsFolder.resolve(pluginRoot.name());

        if (pluginRoot.path().getNameCount() == 0) {
            archive.extract(targetFolder, progressListener);
        } else {
            archive.extract(targetFolder, pluginRoot.path(), progressListener);
        }
    }

    /**
     * Finds the root folder of a plugin archive by locating the folder that contains
     * platform-specific subdirectories (64, mac_x64, win_x64, lin_x64, etc.).
     *
     * @param archive the archive to analyze
     * @return a PluginRoot containing the path and name, or empty if not found
     */
    private Optional<PluginRoot> findPluginRoot(Archive archive) {
        // Check the first .xpl file to determine the root
        return archive.getPaths().stream()
                .filter(p -> p.toString().toLowerCase().endsWith(".xpl"))
                .findFirst()
                .map(Path::getParent)
                .map(parent -> {
                    String parentName = parent.getFileName().toString();

                    // Check if parent matches platform patterns
                    if (!isPlatformFolder(parentName)) {
                        // Parent is the plugin root
                        return new PluginRoot(parent, parentName);
                    }
                    // Parent is a platform folder, so root is grandparent
                    Path grandparent = parent.getParent();
                    if (grandparent == null || grandparent.getNameCount() == 0) {
                        // Grandparent is archive root - use archive filename as name
                        return new PluginRoot(grandparent != null ? grandparent : Path.of(""), deriveNameFromArchive(archive));
                    }
                    // Grandparent is a named folder
                    return new PluginRoot(grandparent, grandparent.getFileName().toString());
                });
    }
    
    // ponytail: uses archive filename as plugin name when root is archive root
    // Upgrade path: parse plugin metadata from .xpl file or manifest if available
    private String deriveNameFromArchive(Archive archive) {
        String name = archive.getSourcePath().getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
    
    private boolean isPlatformFolder(String folderName) {
        String lower = folderName.toLowerCase();
        return lower.equals("64") || 
               lower.equals("32") ||
               lower.matches("^(mac|win|lin)_x(64|32)$");
    }

    @SneakyThrows
    public void uninstallPlugin(Plugin plugin) {
        plugin.uninstall();
        reload();
    }
}
