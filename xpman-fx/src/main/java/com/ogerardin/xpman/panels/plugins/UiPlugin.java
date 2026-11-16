package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.Uninstallable;
import com.ogerardin.xplane.XPlaneVariant;
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
        return plugin.isUpdateAvailable();
    }

    @Override
    public String getUpdateTooltip() {
        return com.ogerardin.xpman.util.jfx.UpdateUtil.updateTooltip(plugin);
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
    @Label("updateActionLabel")
    @EnabledIf("updateAvailable")
    public void update() {
        com.ogerardin.xpman.util.jfx.UpdateUtil.showUpdates(plugin, () -> plugin.getXPlane().getPluginManager().reload());
    }

    @Override public String getUpdateActionLabel() { return com.ogerardin.xpman.util.jfx.UpdateUtil.actionLabel(plugin); }

    @SuppressWarnings("unused")
    @Order(100)
    @Label("'Skunkcrafts: Locked by developer'")
    @EnabledIf("updateLocked")
    public void showUpdateLock() {
        new Alert(Alert.AlertType.INFORMATION,
                "This plugin is currently locked by the developer. " +
                "The developer may be uploading files. Please try again later.")
                .showAndWait();
    }

    @SuppressWarnings("unused")
    @Order(105)
    @Label("'Trim to ' + xPlane.variant.name()")
    @EnabledIf("hasNonApplicableBinaries")
    @Confirm(value = """
            'The following files will be removed:\n\n' + nonApplicableFilesList + '\n\n' +
            (skunkcraftsManaged ?
                'These files will also be added to skunkcrafts_updater_ignore.txt so the updater does not restore them.\n' +
                'To restore all files, delete skunkcrafts_updater_ignore.txt and run the updater.\n\n' : '') +
            'This action cannot be undone.'
            """, alertType = Alert.AlertType.CONFIRMATION)
    @OnSuccess("reload()")
    public void trimToVariant() throws IOException {
        plugin.trimToVariant(plugin.getXPlane().getVariant());
    }

    public boolean isHasNonApplicableBinaries() {
        return !plugin.getNonApplicableXplFiles(plugin.getXPlane().getVariant()).isEmpty();
    }

    public boolean isSkunkcraftsManaged() {
        return plugin.isSkunkcraftsManaged();
    }

    public int getNonApplicableBinaryCount() {
        return plugin.getNonApplicableXplFiles(plugin.getXPlane().getVariant()).size();
    }

    public String getNonApplicableFilesList() {
        var files = plugin.getNonApplicableXplFiles(plugin.getXPlane().getVariant());
        if (files.isEmpty()) {
            return "None";
        }
        
        var relativePaths = files.stream()
                .map(file -> plugin.getBaseFolder().relativize(file).toString())
                .sorted()
                .map(path -> "  • " + path)
                .toList();
        
        return String.join("\n", relativePaths);
    }

    @SuppressWarnings("unused")
    @Order(110)
    public void properties() {
        PropertiesStage.show("plugin", getName(), plugin);
    }
}
