package com.ogerardin.xpman.panels.scenery.rules;

import com.ogerardin.xpman.scenery_organizer.RegexSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.cell_factory.ValidatingEditingCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.util.List;
import java.util.regex.Pattern;

public class RulesController {

    @FXML
    private TableView<SceneryClass> tableView;
    @FXML
    private TableColumn<SceneryClass, Integer> priorityColumn;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;

    @FXML
    private TableColumn<SceneryClass, String> regexColumn;
    @FXML
    private TableColumn<SceneryClass, String> nameColumn;
    @FXML
    private Button deleteButton;

    public void setItems(List<SceneryClass> items) {
        tableView.getItems().setAll(items);
    }

    @FXML
    public void initialize() {
        ReadOnlyIntegerProperty selectedIndexProperty = tableView.getSelectionModel().selectedIndexProperty();
        upButton.disableProperty().bind(selectedIndexProperty.lessThanOrEqualTo(0));
        downButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            int index = selectedIndexProperty.get();
            return index < 0 || index + 1 >= tableView.getItems().size();
        }, selectedIndexProperty, tableView.getItems()));

        deleteButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            SceneryClass sel = tableView.getSelectionModel().getSelectedItem();
            return sel == null || !sel.isEditable();
        }, tableView.getSelectionModel().selectedItemProperty(), tableView.getItems()));

        tableView.setRowFactory(tv -> {
            TableRow<SceneryClass> row = new TableRow<>();
            row.itemProperty().addListener((_, _, item) ->
                    row.setStyle(item != null && item.isBuiltin() ? "-fx-font-style: italic; -fx-opacity: 0.8;" : null));
            return row;
        });

        nameColumn.setCellFactory(column -> new TextFieldTableCell<SceneryClass, String>(new DefaultStringConverter()) {
            @Override
            public void startEdit() {
                if (!getTableRow().getItem().isEditable()) return;
                super.startEdit();
            }
        });
        nameColumn.setOnEditCommit(event -> {
            var item = (RegexSceneryClass) event.getRowValue();
            item.setName(event.getNewValue());
        });

        regexColumn.setCellFactory(column -> new ValidatingEditingCell<SceneryClass>(this::isValidRegex) {
            @Override
            public void startEdit() {
                if (!getTableRow().getItem().isEditable()) return;
                super.startEdit();
            }
        });
        regexColumn.setOnEditCommit(event -> {
            var item = (RegexSceneryClass) event.getRowValue();
            item.setRegex(event.getNewValue());
        });

        tableView.setEditable(true);
    }

    private boolean isValidRegex(String s) {
        try {
            Pattern.compile(s);
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    @FXML
    private void add() {
        final int newPos = tableView.getItems().size();
        tableView.getItems().add(newPos, new RegexSceneryClass("New"));
        tableView.getSelectionModel().select(newPos);
        tableView.scrollTo(newPos);
        tableView.layout();
        tableView.edit(newPos, nameColumn);
    }

    @FXML
    private void delete() {
        int index = tableView.getSelectionModel().getSelectedIndex();
        tableView.getItems().remove(index);
    }

    @FXML
    private void up() {
        moveRow(-1);
    }

    @FXML
    private void down() {
        moveRow(+1);
    }

    private void moveRow(int delta) {
        int index = tableView.getSelectionModel().getSelectedIndex();
        tableView.getItems().add(index + delta, tableView.getItems().remove(index));
        tableView.getSelectionModel().clearAndSelect(index + delta);
    }

    @FXML
    private void restoreDefaults() {
        setItems(new SceneryOrganizer().getOrderedSceneryClasses());
    }

    public List<SceneryClass> getItems() {
        return tableView.getItems();
    }
}
