package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.panels.scenery.rules.SceneryClassesController;
import com.ogerardin.xpman.panels.scenery.wizard.OrganizeWizard;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import com.ogerardin.xplane.util.platform.Platforms;
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

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SceneryController extends Controller {

    private final XPlaneProperty xPlaneProperty;
    private final SceneryOrganizer sceneryOrganizer;
    private final XPmanFX mainController;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiScenery> sceneryTable;

    @FXML
    private TableColumn<UiScenery, Integer> rankColumn;

    private final IntrospectingContextMenuTableRowFactory<UiScenery> rowFactory =
            new IntrospectingContextMenuTableRowFactory<>(this);

    private ManagerItemsObservableList<SceneryPackage, UiScenery> uiItems;

    public SceneryController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        sceneryOrganizer = mainController.getSceneryOrganizer();
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // add context menu to table rows
        sceneryTable.setRowFactory(rowFactory);

        sceneryTable.setPlaceholder(new EmptyState("fth-map", "No scenery to show"));

        // sort by rank with nulls last (rank is null if scenery is disabled)
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

    public void reload() {
        rowFactory.clearCache();
        uiItems.reload();
    }

    public void installScenery() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.SCENERY);
        wizard.showAndWait();
        reload();
    }

    @FXML
    private void organize() {
        XPlane xPlane = xPlaneProperty.get();
        OrganizeWizard wizard = new OrganizeWizard(xPlane, sceneryOrganizer);
        wizard.showAndWait();
        reload();
    }

    @FXML
    @SneakyThrows
    private void openSceneryClasses() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sceneryClasses.fxml"));
        loader.setControllerFactory(type -> {
            if (type == SceneryClassesController.class) {
                return new SceneryClassesController(mainController);
            }
            try {
                return type.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Pane pane = loader.load();
        SceneryClassesController controller = loader.getController();
        controller.setSceneryController(this);
        Stage stage = new Stage();
        stage.setTitle("Scenery classes");
        stage.setScene(new Scene(pane));
        stage.initOwner(mainController.getPrimaryStage());
        stage.show();
    }

    @FXML
    private void openSceneryPacksIni() {
        XPlane xPlane = xPlaneProperty.get();
        Path path = xPlane.getPaths().customScenery().resolve("scenery_packs.ini");
        Platforms.getCurrent().openFile(path);
    }
}
