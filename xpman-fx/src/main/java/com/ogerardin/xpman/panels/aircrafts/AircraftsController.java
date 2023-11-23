package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.aircrafts.AircraftManager;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.util.Streams;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.Filter;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class AircraftsController extends Controller {

    private static final Label PLACEHOLDER = new Label("No aircrafts to show");

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ComboBox<Filter<Aircraft>> filterCombo;

    @FXML
    private ToolBar toolbar;

    @FXML
    private TableView<UiAircraft> aircraftsTable;

    private ManagerItemsObservableList<Aircraft, UiAircraft> uiItems;
    private FilteredList<UiAircraft> filteredUiItems;

    public AircraftsController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        // bind filter combo items to XPlane
        filterCombo.itemsProperty().bind(
            Bindings.createObjectBinding(
                () -> FXCollections.observableList(buildFilters(xPlaneProperty.get())),
                xPlaneProperty
            )
        );

        // build observable list of UiAircraft
        uiItems = new ManagerItemsObservableList<>(
                this.xPlaneProperty,
                XPlane::getAircraftManager,
                UiAircraft::new
        );
        filteredUiItems = new FilteredList<>(uiItems);
        aircraftsTable.setItems(filteredUiItems);

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

    /**
     * Returns a list of aircraft filters to display in the filter combo
     */
    private static List<Filter<Aircraft>> buildFilters(XPlane xPlane) {
        if (xPlane == null) {
            return Collections.emptyList();
        }
        return Streams.concat(
                    Stream.of(Filter.all()),
                    getCategoryFilters().stream(),
                    getStudioFilters(xPlane).stream(),
                    Stream.of(new Filter<Aircraft>("Studio ≠ Laminar Research",
                            aircraft -> !aircraft.getStudio().equals("Laminar Research")))
                ).toList();
    }

    /**
     * Returns a list of aircraft filters by studio
     */
    private static List<Filter<Aircraft>> getStudioFilters(XPlane xPlane) {
        AircraftManager aircraftManager = xPlane.getAircraftManager();
        return aircraftManager.getStudios().stream()
                .map(studio -> new Filter<Aircraft>("Studio: " + studio,
                        aircraft -> aircraft.getStudio().equals(studio)))
                .toList();
    }

    /**
     * Returns a list of aircraft filters by category
     */
    private static List<Filter<Aircraft>> getCategoryFilters() {
        return Arrays.stream(Aircraft.Category.values())
                .map(category -> new Filter<Aircraft>("Category: " + category,
                        aircraft -> aircraft.getCategory() == category))
                .toList();
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

    public void applyFilter() {
        Predicate<Aircraft> predicate = filterCombo.getValue().getPredicate();
        filteredUiItems.setPredicate(uiAircraft -> predicate.test(uiAircraft.getAircraft()));
    }
}
