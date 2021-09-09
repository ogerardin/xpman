package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.navdata.NavDataGroup;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.panels.TreeTableViewManagerEventListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NavDataController {

    private static final Label PLACEHOLDER = new Label("No nav data to show");

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    private EventListener<ManagerEvent<NavDataSet>> eventListener;


    @FXML
    private TreeTableColumn<UiNavDataItem, Boolean> existsColumn;

    @FXML
    private TreeTableView<UiNavDataItem> treeTableView;

    public NavDataController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
//        mainController.xPlaneProperty().addListener((observable, oldValue, newValue) -> updateView(newValue));
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    @FXML
    public void initialize() {
        treeTableView.placeholderProperty().setValue(PLACEHOLDER);

        eventListener = new TreeTableViewManagerEventListener<>(treeTableView,
                navDataSet -> treeItem(new NavDataGroup("Nav data sets", navDataSet)));
    }

    public void reload() {
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            treeTableView.setRoot(null);
        } else {
            NavDataManager navDataManager = xPlane.getNavDataManager();
            navDataManager.registerListener(eventListener);
            navDataManager.reload();
        }
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

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.NAVDATA);
        wizard.showAndWait();
        reload();
    }


}
