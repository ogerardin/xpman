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

    private final Path folder;

    private final String name;

    public Plugin(Path folder) {
        this(folder, folder.getFileName().toString());
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
