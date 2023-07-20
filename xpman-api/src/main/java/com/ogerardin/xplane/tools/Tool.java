package com.ogerardin.xplane.tools;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A tool is an external application that can be run to provide X-Plane related functionality.
 * Tools managed by X-Plane Manager are always installed in the {X-Plane root folder}/Resources/tools folder.
 * A tool is described by a {@link Manifest}.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public abstract sealed class Tool permits InstallableTool, InstalledTool {

    @NonNull
    private final String name;

    private final Manifest manifest;

    public abstract boolean isInstallable();

    public abstract boolean isInstalled();

    public abstract boolean isRunnable();

    public abstract String getVersion();

}
