package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.InstallableTool;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import lombok.Data;
import lombok.experimental.Delegate;

/** UI wrapper around a domain {@link Tool} that exposes install/uninstall actions for the UI layer. */
@SuppressWarnings("ClassCanBeRecord")
@Data
public class UiTool {

    @Delegate
    private final Tool tool;

    private final XPlane xPlane;

    @EnabledIf("installable")
    public void install() {
        UiToolUtil.installTool(xPlane, (InstallableTool) tool);
    }

    @EnabledIf("installed")
    public void uninstall() {
        UiToolUtil.uninstallTool(xPlane, (InstalledTool) tool);
    }

}
