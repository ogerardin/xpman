package com.ogerardin.xplane.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.ProgressListener;

public interface ToolInstaller {

    void install(XPlane xPlane, ProgressListener progressListener);
}
