package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.javafx.panels.TableViewController;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.ToString;

import java.util.stream.Collectors;

public class SceneryController extends TableViewController<XPlaneInstance, UiScenery> {

    @FXML
    @ToString.Exclude
    private TableView<UiScenery> sceneryTable;

    public SceneryController(XPlaneInstanceProperty xPlaneInstanceProperty) {
        super(
                xPlaneInstanceProperty,
                xPlaneInstance -> xPlaneInstance.getSceneryManager().getPackages().stream()
                .map(sceneryPackage -> new UiScenery(sceneryPackage, xPlaneInstance))
                .collect(Collectors.toList())
        );
    }

    @FXML
    public void initialize() {
        setTableView(sceneryTable);

        sceneryTable.setRowFactory(tableView -> {
            final TableRow<UiScenery> row = new TableRow<>();
            setContextMenu(row, UiScenery.class);
            return row;

        });
    }

    public void installScenery() {
    }
}
