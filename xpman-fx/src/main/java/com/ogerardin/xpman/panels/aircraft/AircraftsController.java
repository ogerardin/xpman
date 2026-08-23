package com.ogerardin.xpman.panels.aircraft;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.AircraftManager;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.util.Streams;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.Filter;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Slf4j
public class AircraftsController extends Controller {

    private static final Label EMPTY_PLACEHOLDER = new Label("No aircraft to show");

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ComboBox<Filter<Aircraft>> filterCombo;

    @FXML
    private TextField searchField;

    @FXML
    private ToolBar toolbar;

    @FXML
    private FlowPane cardsPane;

    @FXML
    private StackPane placeholderPane;

    private final StringProperty searchText = new SimpleStringProperty("");

    private ManagerItemsObservableList<Aircraft, UiAircraft> uiItems;
    private FilteredList<UiAircraft> filteredUiItems;

    private final GenericContextMenuFactory<UiAircraft> cardMenuFactory =
            new GenericContextMenuFactory<>(this);

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
        filteredUiItems.predicateProperty().bind(
                Bindings.createObjectBinding(this::combinedPredicate, filterCombo.valueProperty(), searchText));

        searchField.textProperty().addListener((__, ___, text) -> searchText.set(text == null ? "" : text));

        // rebuild the card grid whenever the filtered list changes
        filteredUiItems.addListener((ListChangeListener<UiAircraft>) __ -> Platform.runLater(this::updateCards));

        // disable toolbar whenever xPlaneProperty is null
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));

        updateCards();
    }

    /**
     * @return the combined predicate for the current filter combo selection and search text
     */
    private Predicate<UiAircraft> combinedPredicate() {
        Filter<Aircraft> filter = filterCombo.getValue();
        Predicate<Aircraft> filterPredicate = filter != null ? filter.getPredicate() : o -> true;
        String needle = searchText.get() == null ? "" : searchText.get().trim().toLowerCase(Locale.ROOT);
        return uiAircraft -> filterPredicate.test(uiAircraft.getAircraft())
                && (needle.isEmpty() || matches(uiAircraft, needle));
    }

    private static boolean matches(UiAircraft uiAircraft, String needle) {
        return contains(uiAircraft.getName(), needle)
                || contains(uiAircraft.getStudio(), needle)
                || contains(uiAircraft.getAuthor(), needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private void updateCards() {
        List<UiAircraft> items = filteredUiItems;
        cardsPane.getChildren().clear();
        items.forEach(uiAircraft -> cardsPane.getChildren().add(new AircraftCardView(uiAircraft, this)));

        boolean loading = uiItems.getLoadingProperty().get();
        boolean empty = items.isEmpty();
        placeholderPane.setVisible(empty || loading);
        placeholderPane.setManaged(empty || loading);
        placeholderPane.getChildren().setAll(loading ? LOADING : EMPTY_PLACEHOLDER);
    }

    GenericContextMenuFactory<UiAircraft> getCardMenuFactory() {
        return cardMenuFactory;
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
}
