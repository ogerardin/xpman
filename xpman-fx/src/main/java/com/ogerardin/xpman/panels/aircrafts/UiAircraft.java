package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.javafx.panels.menu.*;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xpman.platform.Platforms;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.net.URL;
import java.nio.file.Path;

@RequiredArgsConstructor
public class UiAircraft {

    @Delegate
    private final Aircraft aircraft;

    private final XPlaneInstance xPlaneInstance;

    @Label("T(com.ogerardin.xpman.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Path file = aircraft.getAcfFile().getFile();
        Platforms.getCurrent().reveal(file);
    }

    @Label("'Enable Aircraft'")
    @EnabledIf("! enabled")
    @RefreshAfter
    public void enable() {
        xPlaneInstance.getAircraftManager().enableAircraft(aircraft);
    }

    @Label("'Disable Aircraft'")
    @EnabledIf("enabled")
    @Confirm
    @RefreshAfter
    public void disable() {
        xPlaneInstance.getAircraftManager().disableAircraft(aircraft);
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key.label")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openInBrowser(url);
    }

}
