package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class ToolsManager extends Manager<Tool> {

    @NonNull
    @Getter
    private final Path toolsFolder;

    private List<Tool> tools = null;

    public ToolsManager(@NonNull XPlane xPlane) {
        super(xPlane);
        this.toolsFolder = xPlane.getPaths().tools();
    }

    public List<Tool> getTools() {
        if (tools == null) {
            loadTools();
        }
        return Collections.unmodifiableList(tools);
    }

    /**
     * Trigger an asynchronous reload of the list.
     */
    public void reload() {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(this::loadTools);
    }

    @SneakyThrows
    @Synchronized
    private void loadTools() {

        log.info("Loading tools...");
        fireEvent(new ManagerEvent.Loading<>());

        // find all installed tools (runnable files under the tools folder)
        Predicate<Path> isRunnable = p -> Platforms.getCurrent().isRunnable(p);
        List<Tool> installedTools = Files.list(toolsFolder)
                .filter(isRunnable)
                .map(this::getTool)
                .toList();
        log.debug("Found {} installed tools", installedTools.size());

        // find available tools (=all manifests except already installed)
        List<Tool> availableTools = ToolManifest.getAllManifests().stream()
                .filter(m -> installedTools.stream().noneMatch(tool -> tool.getManifest() == m))
                .map(Tool::new)
                .toList();
        log.debug("Found {} available tools", availableTools.size());

        tools = Stream.concat(installedTools.stream(), availableTools.stream()).toList();

        log.info("Loaded {} tools", this.tools.size());
        fireEvent(new ManagerEvent.Loaded<>(this.tools));
    }

    @SneakyThrows
    private Tool getTool(Path path) {
        Optional<ToolManifest> maybeManifest = ToolManifest.getAllManifests().stream()
                .filter(manifest -> path.endsWith(manifest.getExeName()))
                .findAny();
        return maybeManifest.map(m -> new Tool(path, m)).orElseGet(() -> new Tool(path));
    }

    public void installTool(Tool tool) {
        if (! tool.isInstallable()) {
            throw new IllegalStateException("Tool must be installable");
        }
        tool.getManifest().getInstaller().accept(xPlane);
        reload();
    }

    @SneakyThrows
    public void uninstallTool(Tool tool) {
        if (! tool.isInstalled()) {
            throw new IllegalStateException("Tool must be installed");
        }
        var fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        fileUtils.moveToTrash(tool.getExecutable().toFile());
        reload();
    }
}
