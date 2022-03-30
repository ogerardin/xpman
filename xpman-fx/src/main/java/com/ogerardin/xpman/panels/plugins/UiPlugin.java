package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.net.URL;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor
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
