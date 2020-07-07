package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.javafx.panels.TableViewController;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.stream.Collectors;

public class PluginsController extends TableViewController<XPlaneInstance, UiPlugin> {

    @FXML
    private TableView<UiPlugin> pluginTable;

    public PluginsController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getPluginManager().getPlugins().stream()
                .map(UiPlugin::new)
                .collect(Collectors.toList())
        );
    }

    @FXML
    public void initialize() {
        setTableView(pluginTable);

        pluginTable.setRowFactory(
                tableView -> {
                    final TableRow<UiPlugin> row = new TableRow<>();
                    setContextMenu(row, UiPlugin.class);
                    return row;
                });

    }

}
