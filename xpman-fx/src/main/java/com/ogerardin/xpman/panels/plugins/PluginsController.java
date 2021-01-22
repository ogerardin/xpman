package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.stream.Collectors;

public class PluginsController extends TableViewController<XPlane, UiPlugin> {

    @FXML
    private TableView<UiPlugin> pluginTable;

    public PluginsController(XPmanFX mainController) {
        super(
                mainController.xPlaneProperty(),
                xPlane -> xPlane.getPluginManager().getPlugins().stream()
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
