package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xpman.util.panels.menu.Label;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xpman.platform.Platforms;
import lombok.Data;
import lombok.experimental.Delegate;

@Data
public class UiScenery {

    @Delegate
    private final SceneryPackage sceneryPackage;

    private final XPlaneInstance xPlaneInstance;

    @Label("T(com.ogerardin.xpman.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(sceneryPackage.getFolder());
    }

}
