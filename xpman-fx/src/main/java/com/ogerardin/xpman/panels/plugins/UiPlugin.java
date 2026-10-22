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
        return plugin.isSkunkcraftsUpdateAvailable();
    }

    @Label("T(com.ogerardin.xplane.util.platform.Platforms).getCurrent().revealLabel()")
    public void reveal() {
        Platforms.getCurrent().reveal(plugin.getXplFile());
    }

    @Label("'Remove macOS quarantine'")
    @EnabledIf("quarantined")
    public void removeQuarantine() {
        Platforms.getCurrent().removeQuarantine(plugin.getBaseFolder());
    }

    @Label("'Enable plugin'")
    @EnabledIf("! enabled")
    @OnSuccess("reload()")
    public void enablePlugin() throws IOException {
        plugin.setEnabled(true);
    }

    @Label("'Disable plugin'")
    @EnabledIf("enabled && ! system")
    @OnSuccess("reload()")
    public void disablePlugin() throws IOException {
        plugin.setEnabled(false);
    }

    @SuppressWarnings("unused")
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

    @ForEach(group = "Links", iterable = "links.entrySet()", itemLabel = "#item.key")
    public void openLink(@Value("#item.value") URL url) {
        Platforms.getCurrent().openUrl(url);
    }

    @ForEach(group = "Manuals", iterable = "manuals.entrySet()", itemLabel = "#item.key")
    public void openManual(@Value("#item.value") Path path) {
        Platforms.getCurrent().openFile(path);
    }

    @OnSuccess("displayInspectionResults(#result)")
    public InspectionResult inspect() {
        return plugin.inspect();
    }

    @SuppressWarnings("unused")
    @Label("'Update via Skunkcrafts Updater'")
    @EnabledIf("skunkcraftsUpdateAvailable")
    @OnSuccess("reload()")
    public void skunkcraftsUpdate() {
        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(plugin).showAndWait();
    }

    @Override
    public Runnable updateAction() {
        return () -> {
            new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(plugin).showAndWait();
            plugin.getXPlane().getPluginManager().reload();
        };
    }

    @SuppressWarnings("unused")
    @Label("'Skunkcrafts: Locked by developer'")
    @EnabledIf("skunkcraftsLocked")
    public void skunkcraftsLocked() {
        new Alert(Alert.AlertType.INFORMATION,
                "This plugin is currently locked by the developer. " +
                "The developer may be uploading files. Please try again later.")
                .showAndWait();
    }

    @SuppressWarnings("unused")
    public void properties() {
        PropertiesStage.show("plugin", getName(), plugin);
    }
}
