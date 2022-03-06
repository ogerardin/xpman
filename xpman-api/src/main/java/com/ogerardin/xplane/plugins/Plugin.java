package com.ogerardin.xplane.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

@Data
@AllArgsConstructor
public class Plugin implements InspectionsProvider<Plugin> {

    private final XPlane xPlane;

    private final Path xplFile;

    private final String name;

    private final String desc;

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

    public String getVersion() {
        return null;
    }

    public String getLatestVersion() {
        return null;
    }

    public Map<String, URL> getLinks() {
        return Collections.emptyMap();
    }

    @Override
    public Inspections<Plugin> getInspections(XPlane xPlane) {
        return Inspections.of();
    }
}
