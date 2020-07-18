package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.platform.Platforms;
import com.ogerardin.xpman.util.panels.menu.*;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.util.List;

@Data
public class UiAircraft {

    @Delegate
    private final Aircraft aircraft;

    private final XPlaneInstance xPlaneInstance;

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xpman.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile());
    }

    @SuppressWarnings("unused")
    @Label("'Enable Aircraft'")
    @EnabledIf("! enabled")
    @OnSuccess("tableView.refresh()")
    public void enable() {
        xPlaneInstance.getAircraftManager().enableAircraft(aircraft);
    }

    @SuppressWarnings("unused")
    @Label("'Disable Aircraft'")
    @EnabledIf("enabled")
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ ' will be moved to ' + xPlaneInstance.rootFolder.relativize(xPlaneInstance.aircraftManager.disabledAircraftFolder) " +
            "+ ' \n\nPress OK to continue.'")
    @OnSuccess("tableView.refresh()")
    public void disable() {
        xPlaneInstance.getAircraftManager().disableAircraft(aircraft);
    }

    @SuppressWarnings("unused")
    @Label("'Move to Trash'")
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ ' will be moved to the trash.\n\nPress OK to continue.'")
    @OnSuccess("tableView.refresh()")
    public void moveToTrash() {
        xPlaneInstance.getAircraftManager().moveAircraftToTrash(aircraft);
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @OnSuccess("displayCheckResults(#result)")
    public List<InspectionMessage> inspect() {
        return xPlaneInstance.getAircraftManager().inspect(aircraft);
    }

}
