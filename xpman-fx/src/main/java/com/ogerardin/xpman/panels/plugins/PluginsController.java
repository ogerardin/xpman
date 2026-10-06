package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.manager.ManagerEvent;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.cell_factory.SwitchTreeCellFactory;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTreeTableRowFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.util.ArrayList;
import java.util.List;

public class PluginsController extends Controller {

    private final XPlaneProperty xPlaneProperty;
    private final BooleanProperty loading = new SimpleBooleanProperty();

    @FXML
    private TreeTableView<PluginRow> pluginTable;

    @FXML
    private TreeTableColumn<PluginRow, Boolean> enabledColumn;

    private final IntrospectingContextMenuTreeTableRowFactory<PluginRow> pluginRowFactory =
            new IntrospectingContextMenuTreeTableRowFactory<>(this);

    public PluginsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        pluginTable.setRoot(new TreeItem<>());
        pluginTable.placeholderProperty().bind(Bindings.when(loading)
                .then((Node) EmptyState.loading("Loading plugins..."))
                .otherwise(new EmptyState("fth-package", "No plugins to show")));
        pluginTable.setRowFactory(pluginRowFactory);
        enabledColumn.setCellFactory(new SwitchTreeCellFactory(this::reload));

        xPlaneProperty.addListener((__, ___, xPlane) -> {
            if (xPlane != null) {
                xPlane.getPluginManager().registerListener(this::onPluginManagerEvent);
            }
        });

        XPlane xPlane = xPlaneProperty.get();
        if (xPlane != null) {
            xPlane.getPluginManager().registerListener(this::onPluginManagerEvent);
            reload();
        }
    }

    private void onPluginManagerEvent(ManagerEvent<Plugin> event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                // rows are kept until LOADED replaces them, so the tree doesn't flash
                // (and an in-flight switch animation isn't cut short) during reloads
                case LOADING -> loading.set(true);
                case LOADED -> {
                    loading.set(false);
                    rebuildTree(event.getItems());
                }
            }
        });
    }

    private void rebuildTree(List<Plugin> plugins) {
        pluginRowFactory.clearCache();
        List<TreeItem<PluginRow>> items = new ArrayList<>();

        for (Plugin plugin : plugins) {
            UiPlugin uiPlugin = new UiPlugin(plugin);
            TreeItem<PluginRow> pluginItem = new TreeItem<>(uiPlugin);

            if (plugin instanceof FlyWithLua flyWithLua) {
                List<FlyWithLuaScript> scripts = flyWithLua.getScripts();
                List<FlyWithLuaScript> enabled = scripts.stream().filter(FlyWithLuaScript::isEnabled).toList();
                List<FlyWithLuaScript> disabled = scripts.stream().filter(FlyWithLuaScript::isDisabled).toList();
                List<FlyWithLuaScript> quarantined = scripts.stream().filter(FlyWithLuaScript::isQuarantined).toList();

                for (FlyWithLuaScript script : enabled) {
                    pluginItem.getChildren().add(new TreeItem<>(new UiFlyWithLuaScript(script)));
                }
                if (!disabled.isEmpty()) {
                    pluginItem.getChildren().add(scriptGroup("Disabled scripts", disabled));
                }
                if (!quarantined.isEmpty()) {
                    pluginItem.getChildren().add(scriptGroup("Quarantined scripts", quarantined));
                }

                pluginItem.setExpanded(true);
                pluginItem.expandedProperty().addListener(__ -> Platform.runLater(pluginTable::refresh));
            }

            items.add(pluginItem);
        }

        pluginTable.getRoot().getChildren().setAll(items);
        // force cell re-population: cells at unchanged indices never re-query their cellValueFactory
        // (TreeItemPropertyValueFactory on a plain getter returns a dead wrapper), so value changes
        // at the same index would otherwise not re-render
        pluginTable.refresh();
    }

    private TreeItem<PluginRow> scriptGroup(String label, List<FlyWithLuaScript> scripts) {
        TreeItem<PluginRow> header = new TreeItem<>(new ScriptGroupHeader(label + " (" + scripts.size() + ")"));
        for (FlyWithLuaScript script : scripts) {
            header.getChildren().add(new TreeItem<>(new UiFlyWithLuaScript(script)));
        }
        header.expandedProperty().addListener(__ -> Platform.runLater(pluginTable::refresh));
        return header;
    }

    public record ScriptGroupHeader(String name) implements PluginRow {
        @Override public String getName() { return name; }
        @Override public String getDesc() { return null; }
        @Override public String getVersion() { return null; }
        @Override public String getLatestVersion() { return null; }
        @Override public boolean isUpdateAvailable() { return false; }
        @Override public boolean isEnabled() { return false; }
        @Override public void setEnabled(boolean enabled) {}
        @Override public boolean getSystem() { return false; }
        @Override public boolean isScript() { return false; }
        @Override public boolean isGroupHeader() { return true; }
    }

    public void reload() {
        XPlane xPlane = xPlaneProperty.get();
        if (xPlane != null) {
            xPlane.getPluginManager().reload();
        }
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane);
        wizard.showAndWait();
    }
}
