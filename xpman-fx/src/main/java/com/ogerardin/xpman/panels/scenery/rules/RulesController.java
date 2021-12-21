package com.ogerardin.xpman.panels.scenery.rules;

import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xpman.util.jfx.ValidatingEditingCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import lombok.var;

import java.util.List;
import java.util.regex.Pattern;

public class RulesController {

    @FXML
    private TableView<SceneryClass> tableView;
    @FXML
    private TableColumn<SceneryClass, Integer> rankColumn;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;

    private SceneryOrganizer sceneryOrganizer;
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
        // Set rules for up/down buttons
        ReadOnlyIntegerProperty selectedIndexProperty = tableView.getSelectionModel().selectedIndexProperty();
        // disable up button if top row or no row selected
        upButton.disableProperty().bind(selectedIndexProperty.lessThanOrEqualTo(0));
        // disable down button if bottom row or no row selected
        downButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            int index = selectedIndexProperty.get();
            return index < 0 || index + 1 >= tableView.getItems().size();
        }, selectedIndexProperty, tableView.getItems()));

        deleteButton.disableProperty().bind(selectedIndexProperty.lessThan(0));

        // Make regex editable
        regexColumn.setCellFactory(column -> new ValidatingEditingCell<>(this::isValidRegex));
        regexColumn.setOnEditCommit(event -> {
            var item = event.getTableView().getItems().get(event.getTablePosition().getRow());
            item.setRegex(event.getNewValue());
        });

        // Make name editable
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            var item = event.getTableView().getItems().get(event.getTablePosition().getRow());
            item.setName(event.getNewValue());
        });

        tableView.setEditable(true);
    }

    private boolean isValidRegex(String s) {
        try {
            Pattern.compile(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @FXML
    private void add() {
        final int newPos = tableView.getItems().size();
        tableView.getItems().add(newPos, new SceneryClass("New"));
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
        // swap items
        tableView.getItems().add(index + delta, tableView.getItems().remove(index));
        // select item at new position
        tableView.getSelectionModel().clearAndSelect(index + delta);
    }

    @FXML
    private void restoreDefaults() {
        final SceneryOrganizer sceneryOrganizer = new SceneryOrganizer();
        setItems(sceneryOrganizer.getOrderedSceneryClasses());
    }

     public void setSceneryOrganizer(SceneryOrganizer sceneryOrganizer) {
        this.sceneryOrganizer = sceneryOrganizer;
        setItems(sceneryOrganizer.getOrderedSceneryClasses());
    }

    public List<SceneryClass> getItems() {
        return tableView.getItems();
    }

}
