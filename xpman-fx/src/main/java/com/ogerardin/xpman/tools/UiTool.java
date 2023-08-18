package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import lombok.Data;
import lombok.experimental.Delegate;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class UiTool {

    @Delegate
    private final Tool tool;

    private final XPlane xPlane;

    @EnabledIf("installable")
    public void install() {
        ToolUtil.installTool(xPlane, tool);
    }

    @EnabledIf("installed")
    public void uninstall() {
        ToolUtil.uninstallTool(xPlane, tool);
    }

    @EnabledIf("runnable")
    public void run() {
        ToolUtil.runTool(xPlane, tool);
    }


}
