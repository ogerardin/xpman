package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.navdata.NavDataGroup;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.util.jfx.panels.TreeTableViewLoadTask;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

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
        TreeTableViewLoadTask<UiNavDataItem> loadTask = new TreeTableViewLoadTask<>(
                treeTableView,
                () -> new UiNavDataItem(getTree(xPlane.getNavDataManager())),
                uiNavDataItem -> treeItem(uiNavDataItem.getNavDataItem())
        );

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    private NavDataItem getTree(NavDataManager navDataManager) {
        NavDataGroup group = new NavDataGroup("Nav data sets");
        List<NavDataSet> navDataSets = navDataManager.getNavDataSets();
        group.setItems(navDataSets);
        return group;
    }

    private TreeItem<UiNavDataItem> treeItem(NavDataItem navDataItem) {
        UiNavDataItem value = new UiNavDataItem(navDataItem);
        TreeItem<UiNavDataItem> treeItem = new TreeItem<>(value);
        List<TreeItem<UiNavDataItem>> children = navDataItem.getChildren().stream()
                .map(this::treeItem)
                .collect(Collectors.toList());
        treeItem.getChildren().addAll(children);
        return treeItem;
    }

}
