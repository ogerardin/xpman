package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xplane.config.plugins.Plugin;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import com.ogerardin.javafx.panels.TableViewController;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import lombok.ToString;

public class PluginsController extends TableViewController<XPlaneInstance, Plugin> {

    @FXML
    @ToString.Exclude
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
