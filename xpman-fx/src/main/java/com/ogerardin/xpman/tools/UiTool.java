package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class UiTool {

    @Delegate
    private final Tool tool;

    private final XPlane xPlane;

    @EnabledIf("installable")
    public void install() {
        //TODO run this as a Task
        xPlane.getToolsManager().installTool(tool);
    }

    @SneakyThrows
    @EnabledIf("installed")
    public void uninstall() {
        //TODO run this as a Task?
        xPlane.getToolsManager().uninstallTool(tool);
    }

    @EnabledIf("runnable")
    public void run() {
        Platforms.getCurrent().startApp(getExecutable());
    }


}
