package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
public class UiTool {

    @Delegate
    private final Tool tool;

    private final ToolsController controller;

    @EnabledIf("installable")
    public void install() {
        controller.installTool(tool);
    }

    @EnabledIf("installed")
    public void uninstall() {
        controller.uninstallTool(tool);
    }

    @EnabledIf("runnable")
    public void run() {
        controller.runTool(tool);
    }


}
