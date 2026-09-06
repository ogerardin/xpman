package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Getter
public class Plugin extends XPlaneObject implements Inspectable {

    private final Path xplFile;

    private final String name;

    private final String desc;

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

    private static Path getBaseFolder(Path xplFile) {
        Path folder = xplFile.getParent();
        if (folder.endsWith("64") || folder.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder;
    }

    protected Path getBaseFolder() {
        return getBaseFolder(xplFile);
    }

    public String getLatestVersion() {
        return null;
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    // ponytail: always returns true, doesn't sync with X-Plane runtime state.
    // Add persistence and toggle when user demands it.
    public boolean isEnabled() {
        return true;
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
}
