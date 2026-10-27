package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.inspection.Inspectable;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.plugins.Plugin;
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

/** UI wrapper around a {@link Plugin} that exposes context-menu actions (enable, disable, uninstall, inspect, Skunkcrafts update, etc.) for the plugins tree table. */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
@Data
public class UiPlugin implements PluginRow {

    @Delegate(excludes = {Inspectable.class, Uninstallable.class})
    final Plugin plugin;
    
    @Override
    public boolean isScript() {
        return false;
    }

    @Override
    public boolean isUpdateAvailable() {
        return plugin.isAnySkunkcraftsUpdateAvailable();
    }

    @Override
    public boolean isBetaUpdateAvailable() {
        return plugin.isSkunkcraftsBetaUpdateAvailable();
    }

    @Override
    public String getUpdateTooltip() {
        return com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil.updateTooltip(plugin);
    }

    @Order(10)
    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(plugin.getXplFile());
    }

    @Order(20)
    @Label("'Remove macOS quarantine'")
    @EnabledIf("quarantined")
    public void removeQuarantine() {
        Platforms.getCurrent().removeQuarantine(plugin.getBaseFolder());
    }

    @Order(30)
    @Label("'Enable plugin'")
    @EnabledIf("! enabled")
    @OnSuccess("reload()")
    public void enablePlugin() throws IOException {
        plugin.setEnabled(true);
    }

    @Order(40)
    @Label("'Disable plugin'")
    @EnabledIf("enabled && ! system")
    @OnSuccess("reload()")
    public void disablePlugin() throws IOException {
        plugin.setEnabled(false);
    }

    @SuppressWarnings("unused")
    @Order(50)
    @Label("'Uninstall plugin'")
    @EnabledIf("! system")
    @Confirm(value = """
            'The entire folder "' + xPlane.baseFolder.relativize(plugin.baseFolder) + '" will be uninstalled.' +
            plugin.uninstallWarningDetails +
            '

Press OK to continue.'
            """, alertType = Alert.AlertType.WARNING)
    public void uninstall() {
        getXPlane().getPluginManager().uninstallPlugin(plugin);
    }

    @Order(60)
    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @Order(70)
    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @Order(80)
    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return plugin.inspect();
    }

    @SuppressWarnings("unused")
    @Override
    @Order(90)
    @Label("'Update…'")
    @EnabledIf("anySkunkcraftsUpdateAvailable")
    public void skunkcraftsUpdate() {
        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(plugin).showAndWait();
        plugin.getXPlane().getPluginManager().reload();
    }

    @SuppressWarnings("unused")
    @Order(100)
    @Label("'Skunkcrafts: Locked by developer'")
    @EnabledIf("skunkcraftsLocked")
    public void skunkcraftsLocked() {
        new Alert(Alert.AlertType.INFORMATION,
                "This plugin is currently locked by the developer. " +
                "The developer may be uploading files. Please try again later.")
                .showAndWait();
    }

    @SuppressWarnings("unused")
    @Order(110)
    public void properties() {
        PropertiesStage.show("plugin", getName(), plugin);
    }
}
