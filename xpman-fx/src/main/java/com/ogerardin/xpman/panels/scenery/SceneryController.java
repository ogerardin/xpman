package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.javafx.panels.TableViewController;
import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPlaneInstanceProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.stream.Collectors;

public class SceneryController extends TableViewController<XPlaneInstance, UiScenery> {

    @FXML
    private TableColumn<UiScenery, Boolean> airportColumn;

    @FXML
    private TableColumn<UiScenery, Boolean> libraryColumn;
    
    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn rankColumn;

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

        sceneryTable.itemsProperty().addListener((observable, oldValue, newValue) -> {
            // initially sort by rank
            rankColumn.setSortType(TableColumn.SortType.ASCENDING);
            sceneryTable.getSortOrder().setAll(rankColumn);
            sceneryTable.sort();
        });
        
        airportColumn.setCellFactory(SceneryController::booleanCellFactory);
        libraryColumn.setCellFactory(SceneryController::booleanCellFactory);
    }

    private static TableCell<UiScenery, Boolean> booleanCellFactory(TableColumn<UiScenery, Boolean> col) {
        return new TableCell<UiScenery, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (value != null) {
                    setText(value ? "Yes" : null);
                }
            }
        };
    }

    public void installScenery() {
    }
}
