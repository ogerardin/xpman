package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneObject;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Getter
public class Plugin extends XPlaneObject implements Inspectable {

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

    public String getLatestVersion() {
        return null;
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

    public boolean isQuarantined() {
        return Platforms.getCurrent().isQuarantined(getBaseFolder());
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
