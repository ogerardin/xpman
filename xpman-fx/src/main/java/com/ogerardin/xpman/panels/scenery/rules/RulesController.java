package com.ogerardin.xpman.panels.scenery.rules;

import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class RulesController {

    @FXML
    private TableView<SceneryClass> tableView;

    @FXML
    private TableColumn<SceneryClass, Integer> rankColumn;

    @FXML
    private Button upButton;
    @FXML
    private Button downButton;

    public void setItems(List<SceneryClass> items) {
        tableView.getItems().setAll(items);
    }

    @FXML
    public void initialize() {
        ReadOnlyIntegerProperty selectedIndex = tableView.getSelectionModel().selectedIndexProperty();
        // disable up button if top row or no row selected
        upButton.disableProperty().bind(selectedIndex.lessThanOrEqualTo(0));
        // disable down button if bottom row or no row selected
        downButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            int index = selectedIndex.get();
            return index < 0 || index + 1 >= tableView.getItems().size();
        }, selectedIndex, tableView.getItems()));
    }

    @FXML
    private void add() {
    }

    @FXML
    private void delete() {
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
}
