package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.InstallableTool;
import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.Order;
import lombok.Data;
import lombok.experimental.Delegate;

/** UI wrapper around a domain {@link Tool} that exposes install/uninstall actions for the UI layer. */
@SuppressWarnings("ClassCanBeRecord")
@Data
public class UiTool {

    @Delegate
    private final Tool tool;

    private final XPlane xPlane;

    @Order(10)
    @EnabledIf("installable")
    public void install() {
        UiToolUtil.installTool(xPlane, (InstallableTool) tool);
    }

    @Order(20)
    @EnabledIf("installed")
    public void uninstall() {
        UiToolUtil.uninstallTool(xPlane, (InstalledTool) tool);
    }

}
