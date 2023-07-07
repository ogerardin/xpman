package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import lombok.Data;
import lombok.experimental.Delegate;

import java.net.URL;
import java.util.List;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Data
public class UiPlugin {

    @Delegate(excludes = Inspectable.class)
    final Plugin plugin;

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(plugin.getXplFile());
    }

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @OnSuccess("displayInspectionResults(#result)")
    public List<InspectionMessage> inspect() {
        return plugin.inspect();
    }

}
