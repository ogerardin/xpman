package com.ogerardin.xpman.panels.plugins;

import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

import java.util.stream.Collectors;

public class PluginsController extends TableViewController<UiPlugin> {

    @FXML
    private TableView<UiPlugin> pluginTable;

    public PluginsController(XPmanFX mainController) {
        super(
                () -> mainController.xPlaneProperty().get().getPluginManager().getPlugins().stream()
                    .map(UiPlugin::new)
                    .collect(Collectors.toList())
        );
        mainController.xPlaneProperty().addListener((observable, oldValue, newValue) -> reload());
    }

    @FXML
    public void initialize() {
        setTableView(pluginTable);
        pluginTable.setRowFactory(new IntrospectingContextMenuRowFactory<>(pluginTable, UiPlugin.class));
    }

}
