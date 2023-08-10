package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.util.jfx.menu.annotation.*;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;

@SuppressWarnings({"unused"})
@Data
public class UiScenery {

    @Delegate(excludes = Inspectable.class)
    private final SceneryPackage sceneryPackage;

    private final XPlane xPlane;

    private final SceneryClass sceneryClass;

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(sceneryPackage.getFolder());
    }

    @Label("'Enable Scenery Package'")
    @EnabledIf("! enabled")
    @OnSuccess("reload()")
    public void enable() {
        xPlane.getSceneryManager().enableSceneryPackage(sceneryPackage);
    }

    //TODO different warning when the package is a library because the library might be used by other scenery
    @Label("'Disable Scenery Package'")
    @EnabledIf("enabled")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to \"' + xPlane.baseFolder.relativize(xPlane.sceneryManager.disabledSceneryFolder) " +
            "+ '\" \n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void disable() {
        xPlane.getSceneryManager().disableSceneryPackage(sceneryPackage);
    }

    @Label("'Move to Trash'")
    @Confirm("'The entire folder \"' + xPlane.baseFolder.relativize(sceneryPackage.folder) " +
            "+ '\" will be moved to the trash.\n" +
            "\n" +
            "Press OK to continue.'")
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlane.getSceneryManager().moveSceneryPackageToTrash(sceneryPackage);
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return sceneryPackage.inspect();
    }


}
