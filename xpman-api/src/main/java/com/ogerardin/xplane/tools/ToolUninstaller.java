package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.install.ProgressListener;

public interface ToolUninstaller {

    void uninstall(InstalledTool tool, ProgressListener progressListener);
}
