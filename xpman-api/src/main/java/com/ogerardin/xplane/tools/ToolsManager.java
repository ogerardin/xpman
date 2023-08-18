package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADED;
import static com.ogerardin.xplane.manager.ManagerEvent.Type.LOADING;

@Getter
@Slf4j
@ToString
public class ToolsManager extends Manager<Tool> {

    @NonNull
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

        // compute subset of manifests that are applicable to current platform and X-Plane version
        List<Manifest> applicableManifests = getManifests().stream()
                .flatMap(manifest -> manifest.unfold().stream())
                // current platform only
                .filter(m -> Optional.ofNullable(m.platform())
                        .map(p -> p == Platforms.getCurrent())
                        .orElse(true)
                )
                // current X-Plane version only
                .filter(m -> Optional.ofNullable(m.xplaneVersion())
                        .map(v -> v == xPlane.getMajorVersion())
                        .orElse(true)
                )
                .toList();
        log.debug("Found {} applicable manifests", applicableManifests.size());

        // find installed tools (=manifests with existing matching file)
        List<InstalledTool> installedTools = applicableManifests.stream()
                // file path exists
                .filter(m -> Files.exists(toolsFolder.resolve(m.file())))
                // additional checks
                .filter(m -> Optional.ofNullable(m.installChecker())
                        .map(checker -> checker.test(toolsFolder.resolve(m.file())))
                        .orElse(true)
                )
                .map(manifest -> new InstalledTool(toolsFolder.resolve(manifest.file()), manifest))
                .toList();
        log.debug("Found {} installed tool(s)", installedTools.size());

        // find available tools (=all manifests except already installed)
        List<InstallableTool> availableTools = applicableManifests.stream()
                // not already installed
                .filter(m -> installedTools.stream().noneMatch(tool -> tool.getManifest() == m))
                .map(InstallableTool::new)
                .toList();
        log.debug("Found {} available tools", availableTools.size());

        items = Stream.concat(installedTools.stream(), availableTools.stream()).toList();

        log.info("Loaded {} tools", items.size());
        fireEvent(ManagerEvent.<Tool>builder().type(LOADED).source(this).items(items).build());
    }

    public void install(Tool tool, ProgressListener progressListener) {
        if (!(tool instanceof InstallableTool installableTool)) {
            throw new IllegalArgumentException("Tool is not an InstallableTool");
        }
        ToolUtils.install(xPlane, installableTool.getManifest().url(), progressListener);

        reload();
    }

    @SneakyThrows
    public void uninstall(Tool tool, ProgressListener consoleController) {
        if (!(tool instanceof InstalledTool installedTool)) {
            throw new IllegalArgumentException("Tool is not an InstalledTool");
        }
        ToolUtils.defaultUninstaller(installedTool, consoleController);
        reload();
    }

    public void launch(Tool tool) {
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

    public Tool getTool(String toolId) {
        return getTools().stream()
                .filter(tool -> tool.getManifest().id().equals(toolId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tool with id " + toolId));
    }
}
