package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.panels.scenery.rules.SceneryClassesController;
import com.ogerardin.xpman.panels.scenery.wizard.OrganizeWizard;
import com.ogerardin.xpman.scenery_organizer.OtherSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import com.ogerardin.xplane.util.platform.Platforms;
import javafx.beans.binding.Bindings;
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
import java.util.Optional;

public class SceneryController extends Controller {

    private final XPlaneProperty xPlaneProperty;
    private final SceneryOrganizer sceneryOrganizer;
    private final XPmanFX mainController;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiSceneryEntry> sceneryTable;

    @FXML
    private TableColumn<UiSceneryEntry, Integer> rankColumn;

    private final IntrospectingContextMenuTableRowFactory<UiSceneryEntry> rowFactory =
            new IntrospectingContextMenuTableRowFactory<>(this);

    private ManagerItemsObservableList<SceneryEntry, UiSceneryEntry> uiItems;

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

        // the table shows the entries in manager order (ini order, then unlisted folders);
        // the rank column sort (ascending, nulls last) reflects that same order
        rankColumn.setSortType(TableColumn.SortType.ASCENDING);
        rankColumn.setComparator(Comparator.nullsLast(Comparator.naturalOrder()));
        sceneryTable.getSortOrder().setAll(Collections.singletonList(rankColumn));

        // set tooltip for "rank" column
        TableViewUtil.setColumnHeaderTooltip(sceneryTable, rankColumn, "The rank of this scenery in scenery_packs.ini");

        // disable the toolbar if we don't have a current X-Plane instance
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));

        uiItems = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getSceneryManager,
                sceneryEntry -> new UiSceneryEntry(
                        sceneryEntry,
                        xPlaneProperty.get(),
                        sceneryClassOf(sceneryEntry))
        );
        sceneryTable.setItems(uiItems);
    }

    /** Null-safe: unresolved entries (no on-disk package) fall back to the "Other" scenery class. */
    private SceneryClass sceneryClassOf(SceneryEntry sceneryEntry) {
        return Optional.ofNullable(sceneryEntry.getSceneryPackage())
                .map(sceneryOrganizer::sceneryClass)
                .orElse(OtherSceneryClass.INSTANCE);
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
