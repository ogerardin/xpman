package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.diag.DiagController;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.panels.scenery.wizard.OrganizeWizard;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SceneryController {

    private final XPlaneProperty xPlaneProperty;
    private final SceneryOrganizer sceneryOrganizer;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn<UiScenery, Integer> rankColumn;

    private ManagerItemsObservableList<SceneryPackage, UiScenery> uiItems;

    public SceneryController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        sceneryOrganizer = loadSceneryOrganizer(mainController.getConfigManager().getConfig());
    }

    @FXML
    public void initialize() {
        // add context menu to table rows
        sceneryTable.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(this));

        // sort bt rank with nulls last (rank is null if scenery is disabled)
        rankColumn.setSortType(TableColumn.SortType.ASCENDING);
        rankColumn.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
        sceneryTable.getSortOrder().setAll(Collections.singletonList(rankColumn));

        // set tooltip for "rank" column
        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn, "The rank of this scenery in scenery_pack.ini");

        // disable the toolbar if we don't have a current X-Plane instance
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));

        uiItems = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getSceneryManager,
                (SceneryPackage sceneryPackage) -> new UiScenery(
                        sceneryPackage,
                        xPlaneProperty.get(),
                        sceneryOrganizer.sceneryClass(sceneryPackage))
        );

        // wrap items in SortedList to allow sorting through the UI (clicking on column header)
        SortedList<UiScenery> sortedUiItems = new SortedList<>(uiItems);
        sortedUiItems.comparatorProperty().bind(sceneryTable.comparatorProperty());
        sceneryTable.setItems(sortedUiItems);
    }

    private SceneryOrganizer loadSceneryOrganizer(XPManPrefs config) {
        final List<SceneryClass> sceneryClasses = config.getSceneryClasses();
        if (sceneryClasses == null) {
            SceneryOrganizer sceneryOrganizer = new SceneryOrganizer();
            config.setSceneryClasses(sceneryOrganizer.getOrderedSceneryClasses());
            return sceneryOrganizer;
        }
        return new SceneryOrganizer(sceneryClasses);
    }

    public void reload() {
        uiItems.reload();
    }

    public void installScenery() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.SCENERY);
        wizard.showAndWait();
        reload();
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public  void displayCheckResults(List<InspectionMessage> results) {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/diag.fxml"));
        Pane pane = loader.load();
        DiagController controller = loader.getController();
        controller.setItems(results);
        Stage stage = new Stage();
        stage.setTitle("Analysis results");
        stage.setScene(new Scene(pane));
        stage.initOwner(this.sceneryTable.getScene().getWindow());
        stage.show();
    }

    @FXML
    private void organize() {
        XPlane xPlane = xPlaneProperty.get();
        OrganizeWizard wizard = new OrganizeWizard(xPlane, sceneryOrganizer);
        wizard.showAndWait();
        reload();
    }
}
