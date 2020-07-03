package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.javafx.panels.menu.Label;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xpman.platform.Platforms;
import lombok.Data;
import lombok.experimental.Delegate;

import java.nio.file.Path;

@Data
public class UiScenery {

    @Delegate
    private final SceneryPackage sceneryPackage;

    private final XPlaneInstance xPlaneInstance;

    @Label("T(com.ogerardin.xpman.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Path folder = sceneryPackage.getEarthNavDataFolder();
        Platforms.getCurrent().reveal(folder);
    }

}
