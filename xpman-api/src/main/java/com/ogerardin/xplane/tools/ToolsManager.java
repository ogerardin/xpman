package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.ogerardin.xplane.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.ManagerEvent.Type.LOADING;

@Slf4j
@ToString
public class ToolsManager extends Manager<Tool> {

    @NonNull
    @Getter
    private final Path toolsFolder;

    @Getter(lazy = true)
    private final List<Manifest> manifests = loadManifests();


    public ToolsManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.toolsFolder = xPlane.getPaths().tools();
    }

    public List<Tool> getTools() {
        if (items == null) {
            loadTools();
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Trigger an asynchronous reload of the list.
     */
    public void reload() {
        AsyncHelper.runAsync(this::loadTools);
    }

    @SneakyThrows
//    @Synchronized
    private void loadTools() {

        log.info("Loading tools...");
        fireEvent(ManagerEvent.<Tool>builder().type(LOADING).source(this).build());

        // find all installed tools (runnable files under the tools folder)
        Predicate<Path> isRunnable = p -> Platforms.getCurrent().isRunnable(p);
        List<InstalledTool> installedTools = new ArrayList<>();
        try (Stream<Path> pathStream = Files.list(toolsFolder)) {
            installedTools.addAll(pathStream
//                    .filter(isRunnable)
                    .map(this::getTool)
                    .toList());
            log.debug("Found {} installed tool(s)", installedTools.size());
        }
        catch (Throwable t) {
            log.warn("Failed to load installed tools: {}", t.toString());
        }

        //TODO installed tools should not be based on the tools folder but on the manifest

        // find available tools (=all manifests except already installed)
        List<InstallableTool> availableTools = getManifests().stream()
                // current platform only
                .filter(m -> m.platform().isCurrent())
                // current X-Plane version only
                .filter(m -> Optional.ofNullable(m.xplaneVersion())
                        .map(v -> v == xPlane.getMajorVersion())
                        .orElse(true)
                )
                // not already installed
                .filter(m -> installedTools.stream().noneMatch(tool -> tool.getManifest() == m))
                .map(InstallableTool::new)
                .toList();
        log.debug("Found {} available tools", availableTools.size());

        items = Stream.concat(installedTools.stream(), availableTools.stream()).toList();

        log.info("Loaded {} tools", items.size());
        fireEvent(ManagerEvent.<Tool>builder().type(LOADED).source(this).items(items).build());
    }

    @SneakyThrows
    private InstalledTool getTool(Path path) {
        Optional<Manifest> maybeManifest = getManifests().stream()
                .filter(manifest -> manifest.installChecker().test(path))
                .findAny();
        return maybeManifest
                .map(m -> new InstalledTool(path, m)) // installed tool from existing manifest
                .orElseGet(() -> new InstalledTool(path)); // installed tool with no manifest
    }

    public void installTool(Tool tool, ProgressListener progressListener) {
        if (!(tool instanceof InstallableTool installableTool)) {
            throw new IllegalArgumentException("Tool is not an InstallableTool");
        }
        ToolUtils.install(xPlane, installableTool.getManifest().url(), progressListener);

        reload();
    }

    @SneakyThrows
    public void uninstallTool(Tool tool, ProgressListener consoleController) {
        if (!(tool instanceof InstalledTool installedTool)) {
            throw new IllegalArgumentException("Tool is not an InstalledTool");
        }
        ToolUtils.defaultUninstaller(installedTool, consoleController);
        reload();
    }

    public void launchTool(Tool tool) {
        if (!(tool instanceof InstalledTool installedTool)) {
            throw new IllegalArgumentException("Tool is not an InstalledTool");
        }
        Platforms.getCurrent().startApp(installedTool.getApp());
    }

    @SneakyThrows
    private List<Manifest> loadManifests() {
        //noinspection ConstantConditions
        Path toolsDir = Paths.get(getClass().getResource("/tools").toURI());
        List<Manifest> manifests = Files.list(toolsDir)
                .map(JsonManifestLoader::loadManifest)
                .filter(Objects::nonNull)
                .toList();
        return manifests;
    }

}
