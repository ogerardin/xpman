package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.plugins.PluginManager;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.panels.TableViewManagerEventListener;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.experimental.Delegate;

public class PluginsController implements EventListener<ManagerEvent<Plugin>> {

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    @Delegate
    private EventListener<ManagerEvent<Plugin>> eventListener;

    @FXML
    private TableView<UiPlugin> pluginTable;

    public PluginsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    public void reload() {
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            pluginTable.setItems(null);
        } else {
            PluginManager pluginManager = xPlane.getPluginManager();
            pluginManager.registerListener(this);
            pluginManager.reload();
        }
    }


    @FXML
    public void initialize() {
        pluginTable.setRowFactory(new IntrospectingContextMenuRowFactory<>(UiPlugin.class, this));

        // create delegate event listener
        eventListener = new TableViewManagerEventListener<>(pluginTable, UiPlugin::new);
    }

}
