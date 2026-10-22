package com.ogerardin.xplane.install;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.util.progress.ProgressListener;

/**
 * Strategy interface for a source from which an add-on (aircraft, plugin, scenery, tool)
 * can be installed into an X-Plane installation.
 *
 * <p>Implementations know how to perform the actual installation (typically unpacking an
 * archive into the correct folder) and are also {@link Inspectable} so that pre-install
 * checks can be run before the wizard's Next button is enabled.</p>
 */
public interface InstallSource extends Inspectable {

    void install(XPlane xPlane, ProgressListener progressListener);

}
