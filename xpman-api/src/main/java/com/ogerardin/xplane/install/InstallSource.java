package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.util.progress.ProgressListener;

public interface InstallSource extends Inspectable {

    void install(XPlane xPlane, ProgressListener progressListener);

}
