package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import com.ogerardin.xplane.util.platform.Platforms;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class Tool implements InspectionsProvider<Tool> {

    private final Path path;

    private final String name;

    public Tool(Path path) {
        this.path = path;
        this.name = path.getFileName().toString();
    }

    void run() {
        Platforms.getCurrent().startApp(path);
    }

    @Override
    public Inspections<Tool> getInspections(XPlane xPlane) {
        return null;
    }
}
