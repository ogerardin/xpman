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

    // FIXME the official method for disabling a scenerypack is described in https://www.x-plane.com/kb/prioritization-scenery-packs/
    // it involves changing SCENERY_PACK to SCENERY_PACK_DISABLED, and not moving it to another folder
    @Label("'Enable Scenery Package'")
    @EnabledIf("sceneryPackage != null && ! sceneryPackage.enabled")
    @OnSuccess("reload()")
    public void enable() {
        xPlane.getSceneryManager().enableSceneryPackage(getSceneryPackage());
    }

    // FIXME the official method for disabling a scenerypack is described in https://www.x-plane.com/kb/prioritization-scenery-packs/
    // it involves changing SCENERY_PACK to SCENERY_PACK_DISABLED, and not moving it to another folder
    @Label("'Disable Scenery Package'")
    @EnabledIf("sceneryPackage != null && sceneryPackage.enabled")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to \"' + xPlane.baseFolder.relativize(xPlane.sceneryManager.disabledSceneryFolder) " +
            "+ '\" \n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void disable() {
        xPlane.getSceneryManager().disableSceneryPackage(getSceneryPackage());
    }

    @Label("'Move to Trash'")
    @EnabledIf("sceneryPackage != null")
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
