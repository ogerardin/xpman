package com.ogerardin.xpman.panels.aircrafts;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.Filter;
import com.ogerardin.xpman.util.jfx.menu.IntrospectingContextMenuTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.function.Predicate;

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
        initFilters(filterCombo);

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

    private void initFilters(ComboBox<Filter<Aircraft>> filterCombo) {
        filterCombo.getItems().add(Filter.all());
        filterCombo.getItems().addAll(
                Arrays.stream(Aircraft.Category.values())
                        .map(category -> new Filter<Aircraft>("Category: " + category,
                                aircraft -> aircraft.getCategory() == category))
                        .toList()
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

    public void applyFilter() {
        Predicate<Aircraft> predicate = filterCombo.getValue().getPredicate();
        filteredUiItems.setPredicate(uiAircraft -> predicate.test(uiAircraft.getAircraft()));
    }
}
