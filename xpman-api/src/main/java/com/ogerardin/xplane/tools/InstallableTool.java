package com.ogerardin.xplane.tools;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class InstallableTool extends Tool {

    /** Constructs a non-installed (available) tool */
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

}
