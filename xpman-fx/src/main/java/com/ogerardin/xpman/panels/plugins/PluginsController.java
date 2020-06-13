package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.config.plugins.Plugin;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.xpman.panels.DefaultPanelController;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class PluginsController extends DefaultPanelController<Plugin> {

    @FXML
    private TableView<Plugin> pluginTable;

    public PluginsController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getPluginManager().getPlugins());
    }

    @FXML
    public void initialize() {
        setTableView(pluginTable);
    }

}
