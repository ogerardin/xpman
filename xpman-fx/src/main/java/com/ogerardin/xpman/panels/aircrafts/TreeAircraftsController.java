package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.aircrafts.Livery;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTreeTableRowFactory;
import com.ogerardin.xpman.util.jfx.panels.TreeTableViewManagerEventListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TreeAircraftsController {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    private EventListener<ManagerEvent<Aircraft>> eventListener;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TreeTableView<UiAircraft> aircraftsTreeTable;

    public TreeAircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    @FXML
    public void initialize() {
        aircraftsTreeTable.placeholderProperty().setValue(PLACEHOLDER);
        aircraftsTreeTable.setRowFactory(new IntrospectingContextMenuTreeTableRowFactory<>(this));

        eventListener = new TreeTableViewManagerEventListener<>(aircraftsTreeTable,
                aircrafts -> treeItem(aircrafts, xPlaneProperty.get()));

        // disable toolbar whenever xPlaneProperty is null
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void reload() {
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            aircraftsTreeTable.setRoot(null);
        } else {
            AircraftManager aircraftManager = xPlane.getAircraftManager();
            aircraftManager.registerListener(eventListener);
            aircraftManager.reload();
        }
    }

    private TreeItem<UiAircraft> treeItem(List<Aircraft> aircrafts, XPlane xPlane) {
        TreeItem<UiAircraft> treeItem = new TreeItem<>();
        List<TreeItem<UiAircraft>> children = aircrafts.stream()
                .map(aircraft -> treeItem(aircraft, xPlane))
                .toList();
        treeItem.getChildren().addAll(children);
        return treeItem;
    }

    private TreeItem<UiAircraft> treeItem(Aircraft aircraft, XPlane xPlane) {
        TreeItem<UiAircraft> treeItem = new TreeItem<>(new UiAircraft(aircraft, xPlane)) {

            private boolean loaded = false;

            @Override
            public boolean isLeaf() {
                return false;
            }

            @Override
            public ObservableList<TreeItem<UiAircraft>> getChildren() {
                if (! loaded) {
                    loaded = true;
                    List<TreeItem<UiAircraft>> liveries = aircraft.getLiveries().stream()
                            .map(livery -> treeItem(xPlane, aircraft, livery))
                            .toList();
                    super.getChildren().addAll(liveries);
                }
                return super.getChildren();
            }
        };
        return treeItem;
    }

    private TreeItem<UiAircraft> treeItem(XPlane xPlane, Aircraft aircraft, Livery livery) {
        UiLivery value = new UiLivery(xPlane, aircraft, livery);
        return new TreeItem<>(value);
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.AIRCRAFT);
        wizard.showAndWait();
        reload();
    }

}
