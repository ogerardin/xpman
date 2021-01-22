package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.stream.Collectors;

public class SceneryController extends TableViewController<XPlane, UiScenery> {

    private final ObservableObjectValue<XPlane> xPlaneProperty;

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
                mainController.xPlaneProperty(),
                xPlane -> xPlane.getSceneryManager().loadPackages().stream()
                        .map(sceneryPackage -> new UiScenery(sceneryPackage, xPlane))
                        .collect(Collectors.toList())
        );
        xPlaneProperty = mainController.xPlaneProperty();
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

        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn,
                "The rank of this scenery in scenery_pack.ini");

        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void installScenery() {
        XPlane xPlane = getPropertyValue();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.SCENERY);
        wizard.showAndWait();
        reload();
    }
}
