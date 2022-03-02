package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.panels.TableViewManagerEventListener;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuTableRowFactory;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class PluginsController {

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    private EventListener<ManagerEvent<Plugin>> eventListener;

    @FXML
    private TableView<UiPlugin> pluginTable;

    public PluginsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    @FXML
    public void initialize() {
        pluginTable.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(UiPlugin.class, this));

        eventListener = new TableViewManagerEventListener<>(pluginTable, UiPlugin::new);
    }

    public void reload() {
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            pluginTable.setItems(null);
        } else {
            PluginManager pluginManager = xPlane.getPluginManager();
            pluginManager.registerListener(eventListener);
            pluginManager.reload();
        }
    }


}
