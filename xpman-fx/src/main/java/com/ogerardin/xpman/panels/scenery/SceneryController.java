package com.ogerardin.xpman.panels.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.panels.scenery.rules.SceneryClassesController;
import com.ogerardin.xpman.scenery_organizer.OtherSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.TableViewUtil;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import com.ogerardin.xplane.util.platform.Platforms;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public class SceneryController extends Controller {

    private final XPlaneProperty xPlaneProperty;
    private final SceneryOrganizer sceneryOrganizer;
    private final XPmanFX mainController;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiSceneryEntry> sceneryTable;

    @FXML
    private javafx.scene.control.Button saveButton;

    @FXML
    private Label unsavedLabel;

    @FXML
    private TableColumn<UiSceneryEntry, Integer> rankColumn;

    private final SceneryRowFactory rowFactory = new SceneryRowFactory(this);

    private ManagerItemsObservableList<SceneryEntry, UiSceneryEntry> uiItems;
    private final BooleanProperty pendingChanges = new SimpleBooleanProperty();

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
        // sorting is disabled so that view order == ini order: row indices can then be used
        // directly as scenery_packs.ini indices by drag-and-drop and the move buttons
        rankColumn.setSortable(false);

        // set tooltip for each column header
        TableViewUtil.setColumnHeaderTooltips(sceneryTable, Map.of(
                "rankColumn", "The rank of this scenery in scenery_packs.ini (entries listed first have higher priority)",
                "statusColumn", "The status of this scenery with respect to scenery_packs.ini: Enabled, Disabled, Folder missing, Token, or blank if not listed",
                "nameColumn", "The scenery folder name; entries with no folder on disk show their ini path or token",
                "versionColumn", "The scenery version, when the scenery type provides a way to determine it",
                "hasAirportColumn", "Whether the scenery contains an airport (presence of an apt.dat file)",
                "libraryColumn", "Whether the scenery is an object library (presence of a library.txt file)",
                "tileCountColumn", "The number of terrain tiles (.dsf files) in the scenery",
                "objCountColumn", "The number of object files (.obj files) in the scenery",
                "classColumn", "The scenery class assigned by the scenery organizer rules"
        ));

        // disable the toolbar if we don't have a current X-Plane instance
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
        saveButton.visibleProperty().bind(pendingChanges);
        unsavedLabel.visibleProperty().bind(pendingChanges);
        unsavedLabel.managedProperty().bind(unsavedLabel.visibleProperty());

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
        pendingChanges.set(false);
        uiItems.reload();
    }

    public void markChanged() {
        pendingChanges.set(true);
    }

    public void refreshTable() {
        sceneryTable.refresh();
        markChanged();
    }

    public void syncAndRefresh() {
        XPlane xPlane = xPlaneProperty.get();
        uiItems.setAll(xPlane.getSceneryManager().getSceneryEntries().stream()
                .map(entry -> new UiSceneryEntry(entry, xPlane, sceneryClassOf(entry)))
                .toList());
        sceneryTable.refresh();
        markChanged();
    }

    @FXML
    private void moveUp() {
        moveSelectedBy(-1);
    }

    @FXML
    private void moveDown() {
        moveSelectedBy(1);
    }

    private void moveSelectedBy(int offset) {
        var selectionModel = sceneryTable.getSelectionModel();
        if (selectionModel.getSelectedItem() != null) {
            moveToIndex(selectionModel.getSelectedItem(), selectionModel.getSelectedIndex() + offset);
        }
    }

    /**
     * Moves the given ini-listed entry to the target index (for ini-listed rows, the row index
     * equals the index in scenery_packs.ini), then syncs the table and re-selects the entry.
     */
    private void moveToIndex(UiSceneryEntry item, int targetIndex) {
        if (xPlaneProperty.get().getSceneryManager().moveTo(item.getSceneryEntry(), targetIndex)) {
            syncAndRefresh();
            sceneryTable.getSelectionModel().select(targetIndex);
        }
    }

    // --- drag-and-drop reordering of ini-listed entries within the scenery table ---

    /** The entry currently being dragged, or null when no drag is in progress. */
    private UiSceneryEntry draggedEntry;

    /** The row currently highlighted as drop target, or null. */
    private TableRow<UiSceneryEntry> dropTargetRow;

    private static final String DROP_TARGET_STYLE = "scenery-row-drop-target";

    /** Only ini-listed entries (including tokens) can be dragged, and only onto other ini-listed rows. */
    private boolean isValidDropTarget(TableRow<UiSceneryEntry> row) {
        return draggedEntry != null && !row.isEmpty()
                && row.getItem().getSceneryEntry().getIniItem() != null;
    }

    void onRowDragDetected(TableRow<UiSceneryEntry> row, MouseEvent event) {
        UiSceneryEntry item = row.getItem();
        if (item != null && item.getSceneryEntry().getIniItem() != null) {
            draggedEntry = item;
            Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(item.getName());
            dragboard.setContent(content);
            event.consume();
        }
    }

    void onRowDragOver(TableRow<UiSceneryEntry> row, DragEvent event) {
        if (isValidDropTarget(row)) {
            event.acceptTransferModes(TransferMode.MOVE);
            setDropTargetRow(row);
            event.consume();
        } else {
            setDropTargetRow(null);
        }
    }

    void onRowDragDropped(TableRow<UiSceneryEntry> row, DragEvent event) {
        UiSceneryEntry dragged = draggedEntry;
        if (dragged != null && isValidDropTarget(row)) {
            moveToIndex(dragged, row.getIndex());
            event.setDropCompleted(true);
            event.consume();
        }
        draggedEntry = null;
        setDropTargetRow(null);
    }

    /** Clears drag state when the drag gesture ends, whether by drop or by cancellation. */
    void onRowDragDone(DragEvent event) {
        draggedEntry = null;
        setDropTargetRow(null);
        event.consume();
    }

    void setDropTargetRow(TableRow<UiSceneryEntry> row) {
        if (dropTargetRow == row) {
            return;
        }
        if (dropTargetRow != null) {
            dropTargetRow.getStyleClass().remove(DROP_TARGET_STYLE);
        }
        dropTargetRow = row;
        if (row != null && !row.getStyleClass().contains(DROP_TARGET_STYLE)) {
            row.getStyleClass().add(DROP_TARGET_STYLE);
        }
    }

    public void installScenery() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.SCENERY);
        wizard.showAndWait();
        reload();
    }

    @FXML
    private void organize() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                """
                        This will add unlisted scenery packages, remove invalid entries, and sort scenery packages by class rank.
                        No change will be saved until you click on the "Save" button\s""");
        confirmation.showAndWait().filter(ButtonType.OK::equals).ifPresent(__ -> {
            XPlane xPlane = xPlaneProperty.get();
            List<SceneryEntry> entries = xPlane.getSceneryManager().getSceneryEntries();
            List<SceneryPackage> packages = entries.stream()
                    .filter(entry -> entry.getSceneryPackage() != null)
                    .filter(entry -> entry.getIniItem() != null || !entry.getSceneryPackage().isSystem())
                    .map(SceneryEntry::getSceneryPackage)
                    .toList();
            List<SceneryPackage> ordered = sceneryOrganizer.apply(packages);
            xPlane.getSceneryManager().organize(ordered);
            markChanged();
        });
    }

    @FXML
    private void save() {
        xPlaneProperty.get().getSceneryManager().save();
        pendingChanges.set(false);
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

    /**
     * Row factory that adds a context menu (via annotation introspection) and applies
     * a style class to rows whose entry has status FOLDER_MISSING.
     */
    private static class SceneryRowFactory extends IntrospectingContextMenuTableRowFactory<UiSceneryEntry> {

        private static final String FOLDER_MISSING_STYLE = "scenery-row-folder-missing";

        private static final String DISABLED_STYLE = "scenery-row-disabled";

        private final SceneryController controller;

        SceneryRowFactory(SceneryController controller) {
            super(controller);
            this.controller = controller;
        }

        @Override
        public TableRow<UiSceneryEntry> call(TableView<UiSceneryEntry> tableView) {
            TableRow<UiSceneryEntry> row = new TableRow<>();
            row.itemProperty().addListener((__, ___, newItem) -> {
                if (newItem == null) {
                    row.setContextMenu(null);
                    row.getStyleClass().remove(FOLDER_MISSING_STYLE);
                    row.getStyleClass().remove(DISABLED_STYLE);
                    return;
                }
                row.setContextMenu(getContextMenu(newItem));
                if (newItem.getStatus() == SceneryEntryStatus.FOLDER_MISSING) {
                    if (!row.getStyleClass().contains(FOLDER_MISSING_STYLE)) {
                        row.getStyleClass().add(FOLDER_MISSING_STYLE);
                    }
                } else {
                    row.getStyleClass().remove(FOLDER_MISSING_STYLE);
                }
                if (newItem.getStatus() == SceneryEntryStatus.IN_INI_DISABLED) {
                    if (!row.getStyleClass().contains(DISABLED_STYLE)) {
                        row.getStyleClass().add(DISABLED_STYLE);
                    }
                } else {
                    row.getStyleClass().remove(DISABLED_STYLE);
                }
            });
            // drag-and-drop reordering
            row.setOnDragDetected(event -> controller.onRowDragDetected(row, event));
            row.setOnDragOver(event -> controller.onRowDragOver(row, event));
            row.setOnDragDropped(event -> controller.onRowDragDropped(row, event));
            row.setOnDragExited(event -> controller.setDropTargetRow(null));
            row.setOnDragDone(controller::onRowDragDone);
            return row;
        }
    }
}
