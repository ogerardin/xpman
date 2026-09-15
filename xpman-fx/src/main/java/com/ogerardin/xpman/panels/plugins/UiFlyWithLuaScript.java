package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import javafx.scene.control.Alert;
import lombok.Data;
import lombok.experimental.Delegate;

import java.nio.file.Path;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Data
public class UiFlyWithLuaScript implements PluginRow {

    @Delegate(excludes = {Inspectable.class, Uninstallable.class})
    final FlyWithLuaScript script;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean getSystem() {
        return false;
    }

    @Override
    public boolean isScript() {
        return true;
    }

    @Override
    public String getLatestVersion() {
        return null;
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(script.getLuaFile());
    }

    public void openInTextEditor() {
        Platforms.getCurrent().openInTextEditor(script.getLuaFile());
    }

    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @Label("'Uninstall script'")
    @Confirm(value = "'The script \"' + script.luaFile.fileName + '\" will be uninstalled.' " +
            "+ '\n\nPress OK to continue.'", alertType = Alert.AlertType.WARNING)
    public void uninstallScript() {
        try {
            script.uninstall();
            script.getXPlane().getPluginManager().reload();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
