package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * A non installed tool (available for download/installation).
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class InstallableTool extends Tool {

    public InstallableTool(Manifest manifest) {
        super(manifest.getName(), manifest);
    }

    public boolean isInstallable() {
        return true;
    }

    public boolean isInstalled() {
        return false;
    }

    public boolean isRunnable() {
        return false;
    }

    @Override
    public String getVersion() {
        return getManifest().getVersion();
    }
}
