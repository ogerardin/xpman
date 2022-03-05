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

    private final Path xplFile;

    private final String name;

    @SuppressWarnings("unused")
    public Plugin(Path xplFile) {
        this(xplFile, computeName(xplFile));
    }

    private static String computeName(Path xplFile) {
        Path folder = xplFile.getParent();
        if (folder.endsWith("64") || folder.endsWith("32")) {
            folder = folder.getParent();
        }
        return folder.getFileName().toString();
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
