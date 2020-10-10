package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NavDataController {

    @FXML
    private TreeTableColumn<UiNavDataItem, Boolean> existsColumn;

    @FXML
    private TreeTableView<UiNavDataItem> treeTableView;

    public NavDataController(XPmanFX mainController) {
        mainController.xPlaneInstanceProperty().addListener((observable, oldValue, newValue) -> updateView(newValue)
        );
    }

    @FXML
    public void initialize() {
        existsColumn.setCellFactory(NavDataController::booleanCellFactory);


    }

    private static TreeTableCell<UiNavDataItem, Boolean> booleanCellFactory(TreeTableColumn<UiNavDataItem, Boolean> col) {
        return new TreeTableCell<UiNavDataItem, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(value ? "Yes" : null);
                }
            }
        };
    }


    private void updateView(XPlaneInstance xPlaneInstance) {

        TreeTableViewLoadTask loadTask = new TreeTableViewLoadTask(treeTableView, xPlaneInstance.getNavDataManager());

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

}
