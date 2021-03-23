package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.util.jfx.panels.TableViewLoadTask;
import com.ogerardin.xpman.util.jfx.panels.menu.IntrospectingContextMenuRowFactory;
import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AircraftsController implements EventListener<ManagerEvent<Aircraft>> {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final ObservableObjectValue<XPlane> xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableColumn<UiAircraft, Path> thumbColumn;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    public AircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload());
    }

    public void reload() {
        AircraftManager aircraftManager = xPlaneProperty.get().getAircraftManager();
        aircraftManager.registerListener(this);
        aircraftManager.reload();
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

    private static final ImageView LOADING
            = new ImageView(new Image(AircraftManager.class.getResource("/loading.gif").toExternalForm()));

    private Node defaultPlaceholder;

    @Override
    public void onEvent(ManagerEvent<Aircraft> event) {
        log.debug("Received event: {}", event);

        if (event instanceof ManagerEvent.Loading) {
            log.debug("Manager is loading items");
            // save placeholder to restore it later
            defaultPlaceholder = aircraftsTable.placeholderProperty().get();
            // replace placeholder with loading animation and clear table so it is displayed
            Platform.runLater(() -> {
                aircraftsTable.placeholderProperty().setValue(LOADING);
                aircraftsTable.setItems(null);
            });

        }
        else if (event instanceof ManagerEvent.Loaded) {
            log.debug("Manager has finished loading items");
            final List<Aircraft> items = ((ManagerEvent.Loaded<Aircraft>) event).getItems();

            // map to UI items
            final List<UiAircraft> uiItems = items.stream()
                    .map(aircraft -> new UiAircraft(aircraft, xPlaneProperty.get()))
                    .collect(Collectors.toList());

            // if the list is not an ObservableList, wrap it in a ObservableListWrapper
            ObservableList<UiAircraft> observableList = (uiItems instanceof ObservableList) ?
                    (ObservableList<UiAircraft>) uiItems : new ObservableListWrapper<>(uiItems);

            // populate the tableView
            Platform.runLater(() -> {
                aircraftsTable.setItems(observableList);
                // reset placeholder
                aircraftsTable.placeholderProperty().setValue(defaultPlaceholder);
            });

        }
    }
}
