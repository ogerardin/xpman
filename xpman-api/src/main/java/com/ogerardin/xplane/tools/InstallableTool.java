package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * An uninstalled tool (available for download/installation).
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class InstallableTool extends Tool {

    public InstallableTool(ToolManifest manifest) {
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
        return getManifest().getAvailableVersionGetter().get();
    }
}
