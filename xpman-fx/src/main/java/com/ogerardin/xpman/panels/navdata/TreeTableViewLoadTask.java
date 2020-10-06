package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.config.navdata.NavDataGroup;
import com.ogerardin.xplane.config.navdata.NavDataItem;
import com.ogerardin.xplane.config.navdata.NavDataManager;
import com.ogerardin.xplane.config.navdata.NavDataSet;
import com.ogerardin.xpman.util.jfx.panels.TableViewLoadTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

//TODO: make this class generic and merge with TableViewLoadTask
@Slf4j
@AllArgsConstructor
class TreeTableViewLoadTask extends Task<Void> {

    private static final ImageView LOADING
            = new ImageView(new Image(TableViewLoadTask.class.getResource("/loading.gif").toExternalForm()));

    private final TreeTableView<UiNavDataItem> treeTableView;
    private final NavDataManager navDataManager;

    @Override
    protected Void call() {
        log.debug("begin load treeTable {}", treeTableView.getId());
        // save placeholder to restore it later
        Node defaultPlaceholder = treeTableView.placeholderProperty().get();
        // replace placeholder with loading animation and clear table so it is displayed
        Platform.runLater(() -> {
            treeTableView.placeholderProperty().setValue(LOADING);
            treeTableView.setRoot(null);
        });

        TreeItem<UiNavDataItem> root = getTree(navDataManager);

        Platform.runLater(() -> {
            treeTableView.setRoot(root);
            // reset placeholder
            treeTableView.placeholderProperty().setValue(defaultPlaceholder);
        });
        log.debug("done load treeTable {}", treeTableView.getId());

        return null;
    }

    private TreeItem<UiNavDataItem> getTree(NavDataManager navDataManager) {
        NavDataGroup group = new NavDataGroup("Nav data sets");
        List<NavDataSet> navDataSets = navDataManager.getNavDataSets();
        group.setItems(navDataSets);
        return treeItem(group);
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
