package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.javafx.panels.menu.*;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xpman.platform.Platforms;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.nio.file.Path;

@Data
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
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ ' will be moved to ' + xPlaneInstance.rootFolder.relativize(xPlaneInstance.aircraftManager.disabledAircraftFolder) " +
            "+ ' \n\nPress OK to continue.'")
    @RefreshAfter
    public void disable() {
        xPlaneInstance.getAircraftManager().disableAircraft(aircraft);
    }

    @Label("'Move to Trash'")
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ ' will be moved to the trash.\n\nPress OK to continue.'")
    @RefreshAfter
    public void moveToTrash() {
        xPlaneInstance.getAircraftManager().moveAircraftToTrash(aircraft);
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

}
