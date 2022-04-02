package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.ProgressListener;
import com.ogerardin.xplane.util.platform.Platforms;
import com.sun.jna.Platform;
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
        List<InstalledTool> installedTools = Files.list(toolsFolder)
                .filter(isRunnable)
                .map(this::getTool)
                .toList();
        log.debug("Found {} installed tools", installedTools.size());

        // find available tools (=all manifests except already installed)
        List<InstallableTool> availableTools = ToolManifest.getAllManifests().stream()
                .filter(m -> m.getPlatform() == Platform.getOSType())
                .filter(m -> installedTools.stream().noneMatch(tool -> tool.getManifest() == m))
                .map(InstallableTool::new)
                .toList();
        log.debug("Found {} available tools", availableTools.size());

        tools = Stream.concat(installedTools.stream(), availableTools.stream()).toList();

        log.info("Loaded {} tools", this.tools.size());
        fireEvent(new ManagerEvent.Loaded<>(this.tools));
    }

    @SneakyThrows
    private InstalledTool getTool(Path path) {
        Optional<ToolManifest> maybeManifest = ToolManifest.getAllManifests().stream()
                .filter(manifest -> manifest.getInstallChecker().test(path))
                .findAny();
        return maybeManifest
                .map(m -> new InstalledTool(path, m))
                .orElseGet(() -> new InstalledTool(path));
    }

    public void installTool(Tool tool, ProgressListener progressListener) {
        if (!(tool instanceof InstallableTool installableTool)) {
            throw new IllegalStateException("Tool must be installable");
        }
        installableTool.getManifest().getInstaller().install(xPlane, progressListener);

        reload();
    }

    @SneakyThrows
    public void uninstallTool(Tool tool, ProgressListener consoleController) {
        if (!(tool instanceof InstalledTool installedTool)) {
            throw new IllegalStateException("Tool must be installed");
        }
        installedTool.getManifest().getUninstaller().uninstall(installedTool, consoleController);
        reload();
    }

    public void launchTool(Tool tool) {
        if (!(tool instanceof InstalledTool installedTool)) {
            throw new IllegalStateException("Tool must be installed");
        }
        Platforms.getCurrent().startApp(installedTool.getApp());
    }
}
