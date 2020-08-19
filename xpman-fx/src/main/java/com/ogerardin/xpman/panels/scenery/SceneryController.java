package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.Collectors;

public class SceneryController extends TableViewController<XPlaneInstance, UiScenery> {

    private final ObservableObjectValue<XPlaneInstance> xPlaneInstanceProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableColumn<UiScenery, Boolean> airportColumn;

    @FXML
    private TableColumn<UiScenery, Boolean> libraryColumn;
    
    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn<UiScenery, Integer> rankColumn;

    @FXML
    private TableColumn<UiScenery, Boolean> enabledColumn;

    public SceneryController(XPmanFX mainController) {
        super(
                mainController.xPlaneInstanceProperty(),
                xPlaneInstance -> xPlaneInstance.getSceneryManager().getPackages().stream()
                        .map(sceneryPackage -> new UiScenery(sceneryPackage, xPlaneInstance))
                        .collect(Collectors.toList())
        );
        xPlaneInstanceProperty = mainController.xPlaneInstanceProperty();
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
        
        enabledColumn.setCellFactory(SceneryController::booleanCellFactory);
        airportColumn.setCellFactory(SceneryController::booleanCellFactory);
        libraryColumn.setCellFactory(SceneryController::booleanCellFactory);

        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn,
                "The rank of this scenery in scenery_pack.ini");

        toolbar.disableProperty().bind(Bindings.isNull(xPlaneInstanceProperty));
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
        //TODO
    }
}
