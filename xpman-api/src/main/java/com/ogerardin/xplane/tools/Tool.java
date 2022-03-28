package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A tool is an external application that can be run to provide X-Plane related functionality.
 * Tools managed by X-Plane Manager are always installed in the {X-Plane root folder}/Resources/tools folder
 * A tool is described by a {@link ToolManifest}.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class Tool implements InspectionsProvider<Tool> {

    @NonNull
    private final String name;

    private final ToolManifest manifest;

    @Override
    public Inspections<Tool> getInspections(XPlane xPlane) {
        return null;
    }

    public abstract boolean isInstallable();

    public abstract boolean isInstalled();

    public abstract boolean isRunnable();

    public abstract String getVersion();

}
