package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.panels.TableViewManagerEventListener;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class AircraftsController {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    public AircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    public void reload() {
        final XPlane xPlane = xPlaneProperty.get();
        if (xPlane == null) {
            aircraftsTable.setItems(null);
        } else {
            AircraftManager aircraftManager = xPlane.getAircraftManager();
            aircraftManager.registerListener(
                    new TableViewManagerEventListener<>(
                            aircraftsTable,
                            (Aircraft aircraft) -> new UiAircraft(aircraft, xPlaneProperty.get()))
            );
            aircraftManager.reload();
        }
    }

    @FXML
    public void initialize() {
        aircraftsTable.placeholderProperty().setValue(PLACEHOLDER);
        aircraftsTable.setRowFactory(new IntrospectingContextMenuRowFactory<>(aircraftsTable, UiAircraft.class));
        // disable toolbar whenever xPlaneProperty is null
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
    }

    public void installAircraft() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.AIRCRAFT);
        wizard.showAndWait();
        reload();
    }

}
