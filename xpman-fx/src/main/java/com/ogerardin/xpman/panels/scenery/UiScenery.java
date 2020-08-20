package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.util.jfx.panels.menu.*;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.scenery.SceneryPackage;
import com.ogerardin.xpman.platform.Platforms;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.util.List;

@Data
public class UiScenery {

    @Delegate
    private final SceneryPackage sceneryPackage;

    private final XPlaneInstance xPlaneInstance;

    @SuppressWarnings("unused")
    @Label("T(com.ogerardin.xpman.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(sceneryPackage.getFolder());
    }

    @SuppressWarnings("unused")
    @Label("'Enable Scenery Package'")
    @EnabledIf("! enabled")
    @OnSuccess("tableView.refresh()")
    public void enable() {
        xPlaneInstance.getSceneryManager().enableSceneryPackage(sceneryPackage);
    }

    //TODO different warning when the package is a library because the library might be used by other scenery
    @SuppressWarnings("unused")
    @Label("'Disable Scenery Package'")
    @EnabledIf("enabled")
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(sceneryPackage.folder) " +
            "+ ' will be moved to ' + xPlaneInstance.rootFolder.relativize(xPlaneInstance.sceneryManager.disabledSceneryFolder) " +
            "+ ' \n\nPress OK to continue.'")
    @OnSuccess("tableView.refresh()")
    public void disable() {
        xPlaneInstance.getSceneryManager().disableSceneryPackage(sceneryPackage);
    }

    @Label("'Move to Trash'")
    @Confirm("'The entire folder ' + xPlaneInstance.rootFolder.relativize(sceneryPackage.folder) " +
            "+ ' will be moved to the trash.\n\nPress OK to continue.'")
    @OnSuccess("reload()")
    public void moveToTrash() {
        xPlaneInstance.getSceneryManager().moveSceneryPackageToTrash(sceneryPackage);
    }

    @SuppressWarnings("unused")
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @OnSuccess("displayCheckResults(#result)")
    public List<InspectionMessage> inspect() {
        return xPlaneInstance.getSceneryManager().apply(sceneryPackage);
    }


}
