package com.ogerardin.xpman.panels.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.Livery;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTreeTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * EXPERIMENTAL
 */
@Slf4j
public class TreeAircraftsController {

    private static final Label PLACEHOLDER = new Label("No aircraft to show");

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TreeTableView<UiAircraft> aircraftsTreeTable;

    private ManagerItemsObservableList<Aircraft, Aircraft> items;

    public TreeAircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        aircraftsTreeTable.placeholderProperty().setValue(PLACEHOLDER);

        aircraftsTreeTable.setRowFactory(new IntrospectingContextMenuTreeTableRowFactory<>(this));

        // we can't set the ManagerItemsObservableList directly as a model of the tree, so we just
        // create it, and we will listen to changes to build the tree model
        items = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getAircraftManager,
                Function.identity()
        );
        items.addListener((ListChangeListener<Aircraft>) change -> {
                    List<? extends Aircraft> aircrafts = change.getList();
                    TreeItem<UiAircraft> root = treeItem(aircrafts, xPlaneProperty.get());
                    root.setExpanded(true);
                    aircraftsTreeTable.setRoot(root);
                }
        );

        // disable toolbar whenever xPlaneProperty is null
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void reload() {
        items.reload();
    }

    private TreeItem<UiAircraft> treeItem(List<? extends Aircraft> aircrafts, XPlane xPlane) {
        TreeItem<UiAircraft> treeItem = new TreeItem<>();
        List<TreeItem<UiAircraft>> children = aircrafts.stream()
                .map(this::treeItem)
                .toList();
        treeItem.getChildren().addAll(children);
        return treeItem;
    }

    private TreeItem<UiAircraft> treeItem(Aircraft aircraft) {
        TreeItem<UiAircraft> treeItem = new TreeItem<>(new UiAircraft(aircraft)) {

            private boolean loaded = false;

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public ObservableList<TreeItem<UiAircraft>> getChildren() {
                if (!loaded) {
                    loaded = true;
                    List<TreeItem<UiAircraft>> liveries = aircraft.getLiveries().stream()
                            .map(livery -> treeItem(aircraft, livery))
                            .toList();
                    super.getChildren().addAll(liveries);
                }
                return super.getChildren();
            }
        };
        return treeItem;
    }

    private TreeItem<UiAircraft> treeItem(Aircraft aircraft, Livery livery) {
        UiLivery value = new UiLivery(aircraft, livery);
        return new TreeItem<>(value);
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.AIRCRAFT);
        wizard.showAndWait();
        reload();
    }

}
