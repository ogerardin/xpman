package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Getter
public class Plugin extends XPlaneObject implements Inspectable, Uninstallable, SkunkcraftsUpdatable {

    private Path xplFile;

    private final String name;

    private final String desc;

    @SuppressWarnings("unused")
    public boolean getSystem()
    {
        return false;
    }

    @Getter(lazy = true)
    private final String version = Platforms.getCurrent().extractPluginVersion(xplFile);

    public Plugin(XPlane xPlane, Path xplFile, String name, String desc) {
        super(xPlane);
        this.xplFile = xplFile;
        this.name = name;
        this.desc = desc;
    }

    @SuppressWarnings("unused")
    public Plugin(XPlane xPlane, Path xplFile) {
        this(xPlane, xplFile, computeName(xplFile), null);
    }

    private static String computeName(Path xplFile) {
        Path folder = getBaseFolder(xplFile);
        return folder.getFileName().toString();
    }

    protected static Path getBaseFolder(Path xplFile) {
        Path folder = xplFile.getParent();
        String folderName = folder.getFileName().toString();
        if (folderName.endsWith("64") || folderName.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder;
    }

    public Path getBaseFolder() {
        return getBaseFolder(xplFile);
    }

    @Getter(lazy = true)
    private final SkunkcraftsConfig skunkcraftsConfig = SkunkcraftsUpdater.findConfig(getSkunkcraftsFolder());

    @Override
    public Path getSkunkcraftsFolder() {
        return getBaseFolder();
    }

    @Getter(lazy = true)
    private final String skunkcraftsLatestVersion = isSkunkcraftsUpdatable()
            ? SkunkcraftsUpdater.fetchRemoteVersion(getSkunkcraftsConfig().moduleUrl())
            : null;

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unused")
    @Getter(lazy = true)
    private final Map<String, Path> manuals = computeManuals();

    @SneakyThrows
    private Map<String, Path> computeManuals() {
        Map<String, Path> foundManuals = new HashMap<>();
        try (Stream<Path> paths = Files.walk(getBaseFolder())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                    .forEach(p -> foundManuals.put(p.getFileName().toString(), p));
        }
        return foundManuals;
    }

    public boolean isEnabled() {
        return !getBaseFolder().startsWith(getXPlane().getPaths().disabledPlugins());
    }

    public void setEnabled(boolean enabled) throws IOException {
        if (enabled == isEnabled()) return;
        Path source = getBaseFolder();
        Path targetBase = enabled ? getXPlane().getPaths().plugins() : getXPlane().getPaths().disabledPlugins();
        Path target = targetBase.resolve(source.getFileName());
        Files.createDirectories(targetBase);
        if (Files.exists(target)) {
            com.sun.jna.platform.FileUtils.getInstance().moveToTrash(target.toFile());
        }
        Files.move(source, target);
        // keep this object truthful for consumers (enabled state, reveal, uninstall)
        // until the reload replaces it with a fresh instance
        this.xplFile = target.resolve(source.relativize(xplFile));
    }

    // referenced by com.ogerardin.xpman.panels.plugins.UiPlugin.removeQuarantine
    @SuppressWarnings("unused")
    public boolean isQuarantined() {
        return Platforms.getCurrent().isQuarantined(getBaseFolder());
    }

    public String getUninstallWarningDetails() {
        return "";
    }

    @Override
    public InspectionResult inspect() {
        return UpdateAvailableInspection.INSTANCE.inspect(this);
    }

    @Override
    public void uninstall() throws IOException {
        Path folder = getBaseFolder();
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());
    }
}
