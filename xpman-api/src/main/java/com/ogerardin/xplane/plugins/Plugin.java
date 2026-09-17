package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.skunkcrafts.WhitelistEntry;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xplane.util.progress.ProgressListener;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Getter
public class Plugin extends XPlaneObject implements Inspectable, Uninstallable, SkunkcraftsUpdatable {

    private final Path xplFile;

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
    private final SkunkcraftsConfig skunkcraftsConfig = SkunkcraftsUpdater.findConfig(getBaseFolder());

    public String getLatestVersion() {
        return getSkunkcraftsLatestVersion();
    }

    @Override
    public boolean isSkunkcraftsUpdatable() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && !cfg.disabled() && !cfg.locked() && cfg.moduleUrl() != null;
    }

    @Override
    public boolean isSkunkcraftsLocked() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && cfg.locked();
    }

    @Override
    public String getSkunkcraftsLatestVersion() {
        if (!isSkunkcraftsUpdatable()) return null;
        return SkunkcraftsUpdater.fetchRemoteVersion(getSkunkcraftsConfig().moduleUrl());
    }

    @Override
    public boolean isSkunkcraftsUpdateAvailable() {
        String latest = getSkunkcraftsLatestVersion();
        return latest != null && !Objects.equals(getVersion(), latest);
    }

    @Override
    public SkunkcraftsUpdateSummary getSkunkcraftsUpdateSummary() {
        if (!isSkunkcraftsUpdatable()) {
            return new SkunkcraftsUpdateSummary(0, 0);
        }
        return SkunkcraftsUpdater.computeFilesToUpdateSummary(getBaseFolder(), getSkunkcraftsConfig());
    }

    @Override
    public void applySkunkcraftsUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException {
        if (!isSkunkcraftsUpdatable()) {
            throw new SkunkcraftsUpdateException("Plugin is not Skunkcrafts-updatable");
        }
        SkunkcraftsUpdater.applyUpdate(getBaseFolder(), getSkunkcraftsConfig(), progress);
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @SuppressWarnings("unused")
    @Getter(lazy = true)
    private final Map<String, Path> manuals = computeManuals();

    @SneakyThrows
    private Map<String, Path> computeManuals() {
        Map<String, Path> manuals = new HashMap<>();
        try (Stream<Path> paths = Files.walk(getBaseFolder())) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".pdf"))
                    .forEach(p -> manuals.put(p.getFileName().toString(), p));
        }
        return manuals;
    }

    // ponytail: always returns true, doesn't sync with X-Plane runtime state.
    // Add persistence and toggle when user demands it.
    public boolean isEnabled() {
        return true;
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
        String version = getVersion();
        String latestVersion = getLatestVersion();
        if (latestVersion != null && !Objects.equals(version, latestVersion)) {
            return InspectionResult.of(
                    InspectionMessage.builder()
                            .severity(Severity.WARN)
                            .object(getName())
                            .message("Update available: " + latestVersion)
                            .build()
            );
        }
        return InspectionResult.empty();
    }

    @Override
    public void uninstall() throws IOException {
        Path folder = getBaseFolder();
        com.sun.jna.platform.FileUtils.getInstance().moveToTrash(folder.toFile());
    }
}
