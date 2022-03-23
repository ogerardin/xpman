package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class Tool implements InspectionsProvider<Tool> {

    private final String name;
    private final ToolManifest manifest;

    @Override
    public Inspections<Tool> getInspections(XPlane xPlane) {
        return null;
    }

    public abstract boolean isInstallable();

    public abstract boolean isInstalled();

    public abstract boolean isRunnable();

    public  String getVersion() {
        return null;
    }


}
