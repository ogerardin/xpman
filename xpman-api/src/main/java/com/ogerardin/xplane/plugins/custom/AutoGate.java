package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@SuppressWarnings("unused")
@Slf4j
public class AutoGate extends XPlaneOrgPlugin {

    private static final String XPLANE_URL = "https://forums.x-plane.org/index.php?/files/file/5038-autogate-plugin";

    public AutoGate(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "AutoGate", "animates jetways and docking guidance systems (DGS)",
                XPLANE_URL);
        IntrospectionHelper.require(isAutoGate());
    }

    private boolean isAutoGate() {
        return getBaseFolder().getFileName().toString().equals("AutoGate");
    }

}
