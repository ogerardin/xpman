package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.util.jfx.menu.annotation.*;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;

@SuppressWarnings({"unused"})
@Data
public class UiSceneryEntry {

    @Delegate
    private final SceneryEntry sceneryEntry;

    private final XPlane xPlane;

    private final SceneryClass sceneryClass;

    public String getSceneryClassName() {
        return sceneryClass.getName();
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    @EnabledIf("sceneryPackage != null")
    public void reveal() {
        Platforms.getCurrent().reveal(getSceneryPackage().getFolder());
    }

    @Label("'Enable Scenery Package'")
    @EnabledIf("iniItem != null && iniItem.disabled")
    @OnSuccess("refreshTable()")
    public void enable() {
        xPlane.getSceneryManager().enable(getSceneryEntry());
    }

    @Label("'Disable Scenery Package'")
    @EnabledIf("iniItem != null && ! iniItem.disabled")
    @OnSuccess("refreshTable()")
    public void disable() {
        xPlane.getSceneryManager().disable(getSceneryEntry());
    }

    @Label("'Add to scenery_packs.ini'")
    @EnabledIf("iniItem == null && sceneryPackage != null && ! sceneryPackage.system")
    @OnSuccess("syncAndRefresh()")
    public void addToIni() {
        xPlane.getSceneryManager().addToIni(getSceneryEntry());
    }

    @Label("'Remove from scenery_packs.ini'")
    @EnabledIf("iniItem != null && sceneryPackage == null")
    @OnSuccess("syncAndRefresh()")
    public void removeFromIni() {
        xPlane.getSceneryManager().removeFromIni(getSceneryEntry());
    }

    @Label("'Move to Trash'")
    @EnabledIf("sceneryPackage != null && ! sceneryPackage.system && ! token")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to the trash.\n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlane.getSceneryManager().moveSceneryPackageToTrash(getSceneryPackage());
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @EnabledIf("sceneryPackage != null")
    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return getSceneryPackage().inspect();
    }

}
