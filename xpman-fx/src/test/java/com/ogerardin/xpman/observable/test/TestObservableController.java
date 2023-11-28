package com.ogerardin.xpman.observable.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.panels.aircraft.UiAircraft;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestObservableController {

    private final ImageView LOADING = new ImageView(new Image(this.getClass().getResource("/img-test/loading.gif").toExternalForm()));

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    @FXML
    public void initialize() {

        final Label PLACEHOLDER = new Label("No aircraft to show");

        ManagerItemsObservableList<Aircraft, UiAircraft> aircrafts = new ManagerItemsObservableList<>(
                TestObservable.xPlaneProperty,
                XPlane::getAircraftManager,
                UiAircraft::new
        );
        aircraftsTable.setItems(aircrafts);

        aircraftsTable.placeholderProperty().bind(
                Bindings.when(aircrafts.getLoadingProperty())
                        .then((Node)LOADING)
                        .otherwise(PLACEHOLDER)
        );

    }

}
