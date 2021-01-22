package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
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
        mainController.xPlaneProperty().addListener((observable, oldValue, newValue) -> updateView(newValue)
        );
    }

    private void updateView(XPlane xPlane) {
        TreeTableViewLoadTask loadTask = new TreeTableViewLoadTask(treeTableView, xPlane.getNavDataManager());

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

}
