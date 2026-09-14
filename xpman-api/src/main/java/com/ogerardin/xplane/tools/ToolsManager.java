package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.Manager;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
                .filter(m -> Files.exists(resolveInstallDir(m).resolve(m.file())))
                // additional checks
                .filter(m -> Optional.ofNullable(m.installChecker())
                        .map(checker -> checker.test(resolveInstallDir(m).resolve(m.file())))
                        .orElse(true)
                )
                .map(manifest -> new InstalledTool(resolveInstallDir(manifest).resolve(manifest.file()), manifest))
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

    /**
     * Install the specified tool and upon success returns a corresponding {@link InstalledTool}
     */
    public InstalledTool install(InstallableTool tool, ProgressListener progressListener) throws ToolsException {
        Manifest manifest = tool.getManifest();
        Path installDir = resolveInstallDir(manifest);
        try {
            ToolUtils.install(manifest.url(), installDir, manifest.file(), progressListener);
            
            Path installedApp = installDir.resolve(manifest.file());
            // Remove quarantine to prevent Gatekeeper blocking on macOS
            Platforms.getCurrent().removeQuarantine(installedApp);
            // Fix executable permissions for .app bundles (lost during zip extraction)
            Platforms.getCurrent().fixAppBundlePermissions(installedApp);
            
            // trigger asynchronous reload
            reload();
            // The reload process will load the newly installed tool at some point in the future,
            // but in case the caller wants to use it right away we return a corresponding InstalledTool instance immediately.
            return InstalledTool.ofInstallable(tool, installDir);
        } catch (IOException | InterruptedException e) {
            throw new ToolsException(e);
        }
    }

    @SneakyThrows
    public void uninstall(InstalledTool tool, ProgressListener consoleController) throws ToolsException {
        ToolUtils.defaultUninstaller(tool, consoleController);
        reload();
    }

    public void launch(InstalledTool tool) throws ToolsException {
        try {
            Platforms.getCurrent().startApp(tool.getApp());
        } catch (Exception e) {
            throw new ToolsException(e);
        }
    }

    private Path resolveInstallDir(Manifest m) {
        Path dir = m.installDir() != null 
            ? xPlane.getBaseFolder().resolve(m.installDir()) 
            : toolsFolder;
        return dir.normalize();
    }

    private static Manifest loadFromResource(Resource resource) {
        try (InputStream is = resource.open()) {
            return JsonManifestLoader.loadManifest(is, resource.getPath());
        } catch (IOException e) {
            log.warn("Failed to loadFromResource manifest from {}", resource.getPath());
            return null;
        }
    }

    @SneakyThrows
    private List<Manifest> loadManifests() {
        // we can't assume /tools is a directory and iterate on files because that won't work
        // from a jar file, so we use ClassGraph to find matching resources
        try (ScanResult scanResult = new ClassGraph().acceptPathsNonRecursive("/tools").scan()) {
            List<Manifest> manifests = scanResult.getResourcesWithExtension("json").stream()
                    .map(ToolsManager::loadFromResource)
                    .filter(Objects::nonNull)
                    .toList();
            return manifests;
        }
    }

    public Tool getTool(String toolId) {
        return getTools().stream()
                .filter(tool -> tool.getManifest().id().equals(toolId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tool with id " + toolId));
    }
}
