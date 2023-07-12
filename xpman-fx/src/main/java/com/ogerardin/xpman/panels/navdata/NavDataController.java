package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.navdata.NavDataGroup;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTreeTableRowFactory;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@Slf4j
public class NavDataController {

    private static final Label PLACEHOLDER = new Label("No nav data to show");

    @NonNull
    private final XPlaneProperty xPlaneProperty;

    @FXML
    private TreeTableView<UiNavDataItem> treeTableView;

    private ManagerItemsObservableList<NavDataSet, NavDataSet> items;

    public NavDataController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        treeTableView.placeholderProperty().setValue(PLACEHOLDER);

        treeTableView.setRowFactory(new IntrospectingContextMenuTreeTableRowFactory<>(this));

        // we can't set the ManagerItemsObservableList directly as a model of the tree, so we just
        // create it, and we will listen to changes to build the tree model
        items = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getNavDataManager,
                Function.identity()
        );
        items.addListener((ListChangeListener<NavDataSet>) change -> {
                    List<? extends NavDataSet> navDataSets = change.getList();
                    // create the root item as a NavDataGroup with model items as children
                    NavDataGroup navDataRoot = new NavDataGroup("Nav data sets", navDataSets);
                    // convert root item to TreeItem<UiNavDataItem> (and all items recursively)
                    TreeItem<UiNavDataItem> root = treeItem(navDataRoot);
                    root.setExpanded(true);
                    treeTableView.setRoot(root);
                }
        );
    }

    private TreeItem<UiNavDataItem> treeItem(NavDataItem navDataItem) {
        UiNavDataItem value = new UiNavDataItem(navDataItem);
        TreeItem<UiNavDataItem> treeItem = new TreeItem<>(value);
        List<TreeItem<UiNavDataItem>> children = navDataItem.getChildren().stream()
                .map(this::treeItem)
                .toList();
        treeItem.getChildren().addAll(children);
        treeItem.setExpanded(true);
        return treeItem;
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.NAVDATA);
        wizard.showAndWait();
        reload();
    }

    public void reload() {
        items.reload();
    }


}
