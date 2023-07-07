package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Inspections;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * A tool is an external application that can be run to provide X-Plane related functionality.
 * Tools managed by X-Plane Manager are always installed in the {X-Plane root folder}/Resources/tools folder
 * A tool is described by a {@link Manifest}.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class Tool implements Inspectable {

    @NonNull
    private final String name;

    private final Manifest manifest;

    @Override
    public List<InspectionMessage> inspect() {
        Inspections<Tool> inspections = Inspections.of();
        return inspections.inspect(this);
    }

    public abstract boolean isInstallable();

    public abstract boolean isInstalled();

    public abstract boolean isRunnable();

    public abstract String getVersion();

}
