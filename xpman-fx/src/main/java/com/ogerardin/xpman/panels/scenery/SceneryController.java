package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.panels.TableViewController;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class SceneryController extends TableViewController<UiScenery> {

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn<UiScenery, Integer> rankColumn;

    public SceneryController(XPmanFX mainController) {
        super(
                () -> loadItems(mainController)
        );
        xPlaneProperty = mainController.xPlaneProperty();
    }

    private static List<UiScenery> loadItems(XPmanFX mainController) {
        XPlane xPlane = mainController.xPlaneProperty().get();
        final List<SceneryPackage> packages = xPlane.getSceneryManager().loadPackages();
        return packages.stream()
                .map(sceneryPackage -> new UiScenery(sceneryPackage, xPlane))
                .collect(Collectors.toList());
    }

    @FXML
    public void initialize() {
        setTableView(sceneryTable);

        sceneryTable.setRowFactory(new IntrospectingContextMenuRowFactory<>(sceneryTable, UiScenery.class));

        sceneryTable.itemsProperty().addListener((observable, oldValue, newValue) -> {
            // initially sort by rank
            rankColumn.setSortType(TableColumn.SortType.ASCENDING);
            //noinspection unchecked
            sceneryTable.getSortOrder().setAll(rankColumn);
            sceneryTable.sort();
        });

        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn, "The rank of this scenery in scenery_pack.ini");

        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void installScenery() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.SCENERY);
        wizard.showAndWait();
        reload();
    }
}
