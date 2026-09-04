package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.install.InstallType;
import com.ogerardin.xplane.navdata.NavDataSet;
import com.ogerardin.xpman.XPmanFX;
import com.ogerardin.xpman.XPlaneProperty;
import com.ogerardin.xpman.install.wizard.InstallWizard;
import com.ogerardin.xpman.panels.Controller;
import com.ogerardin.xpman.panels.ManagerItemsObservableList;
import com.ogerardin.xpman.util.jfx.EmptyState;
import com.ogerardin.xpman.util.jfx.menu.GenericContextMenuFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for the nav data panel: a vertical stack of {@link NavDataSetCardView}
 * cards, one per {@link NavDataSet}, mirroring the aircraft panel structure.
 */
@Slf4j
public class NavDataController extends Controller {

    private final XPlaneProperty xPlaneProperty;

    @FXML
    private ToolBar toolbar;

    @FXML
    private VBox cardsPane;

    @FXML
    private StackPane placeholderPane;

    private ManagerItemsObservableList<NavDataSet, UiNavDataItem> uiItems;

    private final GenericContextMenuFactory<UiNavDataItem> cardMenuFactory =
            new GenericContextMenuFactory<>(this);

    public NavDataController(XPmanFX mainController) {
        xPlaneProperty = mainController.xPlaneProperty();
    }

    @FXML
    public void initialize() {
        uiItems = new ManagerItemsObservableList<>(
                xPlaneProperty,
                XPlane::getNavDataManager,
                UiNavDataItem::new
        );
        uiItems.addListener((ListChangeListener<UiNavDataItem>) __ -> Platform.runLater(this::updateCards));
        toolbar.disableProperty().bind(Bindings.isNull(xPlaneProperty));
        updateCards();
    }

    private void updateCards() {
        cardMenuFactory.clearCache();
        cardsPane.getChildren().clear();
        for (int i = 0; i < uiItems.size(); i++) {
            cardsPane.getChildren().add(new NavDataSetCardView(uiItems.get(i), i + 1, uiItems.size(), this));
        }

        boolean loading = uiItems.getLoadingProperty().get();
        boolean empty = uiItems.isEmpty();
        placeholderPane.setVisible(empty || loading);
        placeholderPane.setManaged(empty || loading);
        placeholderPane.getChildren().setAll(loading
                ? EmptyState.loading("Loading nav data...")
                : new EmptyState("fth-navigation", "No nav data to show"));
    }

    GenericContextMenuFactory<UiNavDataItem> getCardMenuFactory() {
        return cardMenuFactory;
    }

    public void install() {
        XPlane xPlane = xPlaneProperty.get();
        InstallWizard wizard = new InstallWizard(xPlane, InstallType.NAVDATA);
        wizard.showAndWait();
        uiItems.reload();
    }

    public void reload() {
        uiItems.reload();
    }
}
