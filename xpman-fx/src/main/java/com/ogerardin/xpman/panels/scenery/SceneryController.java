package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryManager;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.config.XPManPrefs;
import com.ogerardin.xpman.diag.DiagController;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.scenery.wizard.OrganizeWizard;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import com.ogerardin.xpman.util.jfx.panels.TableViewManagerEventListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
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

    private final ObservableObjectValue<XPlane> xPlaneProperty;
    private final SceneryOrganizer sceneryOrganizer;

    private EventListener<ManagerEvent<SceneryPackage>> eventListener;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn<UiScenery, Integer> rankColumn;

    public SceneryController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());

        sceneryOrganizer = loadSceneryOrganizer(mainController.getConfigManager().getConfig());
    }

    @FXML
    public void initialize() {
        // add context menu to table rows
        sceneryTable.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(this));

        // sort nulls last for "rank" column (rank is null if scenery is disabled)
        rankColumn.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));

        // set tooltip for "rank" column
        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn, "The rank of this scenery in scenery_pack.ini");

        // when items are assigned to the table, make sure the table is initially sorted by rank
        sceneryTable.itemsProperty().addListener((observable, oldValue, newValue) -> {
            rankColumn.setSortType(TableColumn.SortType.ASCENDING);
            sceneryTable.getSortOrder().setAll(Collections.singletonList(rankColumn));
            sceneryTable.sort();
        });

        eventListener = new TableViewManagerEventListener<>(
                sceneryTable,
                (SceneryPackage sceneryPackage) -> new UiScenery(
                        sceneryPackage,
                        xPlaneProperty.get(),
                        sceneryOrganizer.sceneryClass(sceneryPackage))
        );

        // disable the toolbar if we don't have a current X-Plane instance
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
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
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            sceneryTable.setItems(null);
        } else {
            SceneryManager sceneryManager = xPlane.getSceneryManager();
            sceneryManager.registerListener(eventListener);
            sceneryManager.reload();
        }
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
