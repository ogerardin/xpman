package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.config.XPlaneInstance;
import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NavDataController {

    @FXML
    private TreeTableView<UiNavDataItem> treeTableView;

    public NavDataController(XPmanFX mainController) {
        mainController.xPlaneInstanceProperty().addListener(
                (observable, oldValue, newValue) -> updateView(newValue)
        );
    }

    private void updateView(XPlaneInstance xPlaneInstance) {

        TreeTableViewLoadTask loadTask = new TreeTableViewLoadTask(treeTableView, xPlaneInstance.getNavDataManager());

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

}
