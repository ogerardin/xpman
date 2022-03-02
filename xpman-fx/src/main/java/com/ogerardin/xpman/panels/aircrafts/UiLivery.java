package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.Livery;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.panels.menu.Label;

public class UiLivery extends UiAircraft {

    private final Livery livery;

    public UiLivery(XPlane xPlane, Aircraft aircraft, Livery livery) {
        super(aircraft, xPlane);
        this.livery = livery;
    }

    @Override
    public String getName() {
        return livery.getName();
    }

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile().resolve(livery.getPath()).normalize());
    }

}
