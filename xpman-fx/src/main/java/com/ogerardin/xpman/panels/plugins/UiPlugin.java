package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.panels.menu.ForEach;
import com.ogerardin.xpman.util.jfx.panels.menu.Label;
import com.ogerardin.xpman.util.jfx.panels.menu.Value;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;

@Data
public class UiPlugin {

    @Delegate
    final Plugin plugin;

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(plugin.getXplFile());
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

}
