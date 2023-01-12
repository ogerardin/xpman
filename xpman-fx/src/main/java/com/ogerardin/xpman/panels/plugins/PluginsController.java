package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class PluginsController {

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private TableView<UiPlugin> pluginTable;

    private ManagerItemsObservableList<Plugin, UiPlugin> uiItems;

    public PluginsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getPluginManager,
                UiPlugin::new
        );
        pluginTable.setItems(uiItems);

        // add context menu
        pluginTable.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(this));
    }

    public void reload() {
        uiItems.reload();
    }


}
