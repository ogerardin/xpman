package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.util.platform.Platforms;
import com.ogerardin.xpman.util.jfx.PropertiesStage;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import com.ogerardin.xpman.util.jfx.menu.annotation.OnSuccess;
import com.ogerardin.xpman.util.jfx.menu.annotation.Order;
import com.ogerardin.xpman.util.jfx.menu.annotation.Value;
import javafx.scene.control.Alert;
import lombok.Data;
import lombok.experimental.Delegate;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/** UI wrapper around a {@link FlyWithLuaScript} that exposes context-menu actions (enable, disable, quarantine, uninstall, etc.) for the plugins tree table. */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Data
public class UiFlyWithLuaScript implements PluginRow {

    @Delegate(excludes = {Inspectable.class, Uninstallable.class})
    final FlyWithLuaScript script;

    @Override
    public boolean getSystem() {
        return false;
    }

    @Override
    public boolean isScript() {
        return true;
    }

    @Override
    public boolean isUpdateAvailable() {
        return script instanceof SkunkcraftsUpdatable updatable && updatable.isAnySkunkcraftsUpdateAvailable();
    }

    @Override
    public boolean isBetaUpdateAvailable() {
        return script instanceof SkunkcraftsUpdatable updatable && updatable.isSkunkcraftsBetaUpdateAvailable();
    }

    @Override
    public String getLatestVersion() {
        return script instanceof SkunkcraftsUpdatable updatable ? updatable.getLatestVersion() : null;
    }

    @Override
    public String getUpdateTooltip() {
        return script instanceof SkunkcraftsUpdatable updatable
                ? com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil.updateTooltip(updatable) : "";
    }

    /** Whether the hosted script is locked by its developer (Skunkcrafts). */
    @SuppressWarnings("unused")
    public boolean isSkunkcraftsLocked() {
        return script instanceof SkunkcraftsUpdatable updatable && updatable.isSkunkcraftsLocked();
    }

    @Order(10)
    @Label("'Enable script'")
    @EnabledIf("disabled")
    @OnSuccess("reload()")
    public void enableScript() throws IOException {
        script.setEnabled(true);
    }

    @Order(20)
    @Label("'Disable script'")
    @EnabledIf("enabled")
    @OnSuccess("reload()")
    public void disableScript() throws IOException {
        script.setEnabled(false);
    }

    @Order(30)
    @Label("'Quarantine script'")
    @EnabledIf("enabled")
    @OnSuccess("reload()")
    public void quarantineScript() throws IOException {
        script.setQuarantined(true);
    }

    @Order(40)
    @Label("'Unquarantine script'")
    @EnabledIf("quarantined")
    @OnSuccess("reload()")
    public void unquarantineScript() throws IOException {
        script.setQuarantined(false);
    }

    @Order(50)
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(script.getLuaFile());
    }

    @Order(60)
    public void openInTextEditor() {
        Platforms.getCurrent().openInTextEditor(script.getLuaFile());
    }

    @Order(70)
    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @Order(80)
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @Order(90)
    @Label("'Uninstall script'")
    @Confirm(value = """
            'The script "' + script.luaFile.fileName + '" will be uninstalled.' +
            '

Press OK to continue.'
            """, alertType = Alert.AlertType.WARNING)
    public void uninstallScript() {
        try {
            script.uninstall();
            script.getXPlane().getPluginManager().reload();
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    @Override
    @Order(100)
    @Label("'Update…'")
    @EnabledIf("updateAvailable")
    public void skunkcraftsUpdate() {
        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard((SkunkcraftsUpdatable) script).showAndWait();
        script.getXPlane().getPluginManager().reload();
    }

    @SuppressWarnings("unused")
    @Order(110)
    @Label("'Skunkcrafts: Locked by developer'")
    @EnabledIf("skunkcraftsLocked")
    public void skunkcraftsLocked() {
        new Alert(Alert.AlertType.INFORMATION,
                "This addon is currently locked by the developer. " +
                "The developer may be uploading files. Please try again later.")
                .showAndWait();
    }

    @SuppressWarnings("unused")
    @Order(120)
    public void properties() {
        PropertiesStage.show("script", script.getLuaFile().getFileName().toString(), script);
    }
}
