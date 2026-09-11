package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import javafx.scene.control.Alert;
import lombok.Data;
import lombok.experimental.Delegate;

@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Data
public class UiFlyWithLuaScript implements PluginRow {

    @Delegate(excludes = Inspectable.class)
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

    @Label("'Move script to Trash'")
    @Confirm(value = "'The script \"' + script.luaFile.fileName + '\" will be moved to the trash.' " +
            "+ '\n\nPress OK to continue.'", alertType = Alert.AlertType.WARNING)
    public void deleteScript() {
        try {
            script.getXPlane().getPluginManager().deleteScript(script);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
