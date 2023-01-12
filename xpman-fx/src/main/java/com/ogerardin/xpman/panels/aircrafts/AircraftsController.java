package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AircraftsController extends Controller {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    private ManagerItemsObservableList<Aircraft, UiAircraft> uiItems;

    public AircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getAircraftManager,
                aircraft -> new UiAircraft(aircraft, xPlaneProperty.get())
        );
        aircraftsTable.setItems(uiItems);

        // add context menu
        aircraftsTable.setRowFactory(new IntrospectingContextMenuTableRowFactory<>(this));

        // disable toolbar whenever xPlaneProperty is null
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));

        // show "loading" animation when aircraft list is loading
        aircraftsTable.placeholderProperty().bind(
                Bindings.when(uiItems.getLoadingProperty())
                        .then((Node) LOADING)
                        .otherwise(PLACEHOLDER)
        );
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.AIRCRAFT);
        wizard.showAndWait();
        uiItems.reload();
    }

    public void reload() {
        uiItems.reload();
    }
}
