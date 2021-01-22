package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.panels.menu.*;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.util.List;

@Data
public class UiAircraft {

    @Delegate
    private final Aircraft aircraft;

    private final XPlane xPlane;

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(aircraft.getAcfFile().getFile());
    }

    @SuppressWarnings("unused")
    @Label("'Enable Aircraft'")
    @EnabledIf("! enabled")
    @OnSuccess("tableView.refresh()")
    public void enable() {
        xPlane.getAircraftManager().enableAircraft(aircraft);
    }

    @SuppressWarnings("unused")
    @Label("'Disable Aircraft'")
    @EnabledIf("enabled")
    @Confirm("'The entire folder <' + xPlane.baseFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ '> will be moved to <' + xPlane.baseFolder.relativize(xPlane.aircraftManager.disabledAircraftFolder) " +
            "+ '>\nThis will impact other aircrafts contained in the same folder (if there are).'" +
            "+ '\n\nPress OK to continue.'")
    @OnSuccess("tableView.refresh()")
    public void disable() {
        xPlane.getAircraftManager().disableAircraft(aircraft);
    }

    @SuppressWarnings("unused")
    @Label("'Move to Trash'")
    @Confirm("'The entire folder <' + xPlane.baseFolder.relativize(aircraft.acfFile.file.parent) " +
            "+ '> will be moved to the trash." +
            "+ '\nThis will impact other aircrafts contained in the same folder (if there are).'" +
            "+ '\n\nPress OK to continue.'")
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlane.getAircraftManager().moveAircraftToTrash(aircraft);
    }

    @SuppressWarnings("unused")
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @SuppressWarnings("unused")
    @OnSuccess("displayCheckResults(#result)")
    public List<InspectionMessage> inspect() {
        return xPlane.getAircraftManager().inspect(aircraft);
    }

}
