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
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTreeTableRowFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

import java.util.ArrayList;
import java.util.List;

public class PluginsController extends Controller {

    private final XPlaneProperty xPlaneProperty;
    private final BooleanProperty loading = new SimpleBooleanProperty();

    @FXML
    private TreeTableView<PluginRow> pluginTable;

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
                case LOADING -> {
                    loading.set(true);
                    pluginTable.getRoot().getChildren().clear();
                }
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
                for (FlyWithLuaScript script : scripts) {
                    pluginItem.getChildren().add(new TreeItem<>(new UiFlyWithLuaScript(script)));
                }
                pluginItem.setExpanded(true);
                pluginItem.expandedProperty().addListener(__ -> Platform.runLater(pluginTable::refresh));
            }

            items.add(pluginItem);
        }

        pluginTable.getRoot().getChildren().setAll(items);
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
