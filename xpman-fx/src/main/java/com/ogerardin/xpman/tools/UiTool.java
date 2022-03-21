package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.Tool;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.panels.menu.EnabledIf;
import lombok.Data;
import lombok.experimental.Delegate;

@SuppressWarnings("ClassCanBeRecord")
@Data
public class UiTool {

    @Delegate
    private final Tool tool;

    @EnabledIf("installable")
    public void install() {
    }

    @EnabledIf("installed")
    public void uninstall() {
    }

    @EnabledIf("runnable")
    public void run() {
        Platforms.getCurrent().startApp(getExecutable());
    }


}
