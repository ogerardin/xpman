package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A non installed tool (available for download/installation).
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public non-sealed class InstallableTool extends Tool {

    public InstallableTool(Manifest manifest) {
        super(manifest.name(), manifest);
    }

    @Override
    public boolean isInstallable() {
        return true;
    }

    @Override
    public boolean isInstalled() {
        return false;
    }

    @Override
    public boolean isRunnable() {
        return false;
    }

    @Override
    public String getVersion() {
        return getManifest().version();
    }
}
