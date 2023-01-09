package com.ogerardin.xpman.observable.test;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
import com.ogerardin.xplane.inspection.InspectionsProvider;
import com.ogerardin.xpman.XPlaneProperty;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

@Slf4j
public class ManagerItemsObservableList<T extends InspectionsProvider<T>, U>
        implements EventListener<ManagerEvent<T>>, ObservableList<U> {

    /**
     * function to obtain the desired manager from an XPlane value
     */
    private final Function<XPlane, Manager<T>> managerGetter;

    /**
     * Function to map an item from the Manager to an item in the ObservableList
     */
    private final Function<T, U> mapper;

    @Getter
    private BooleanProperty loadingProperty = new SimpleBooleanProperty();

    @Delegate
    private ObservableList<U> observableList = FXCollections.observableArrayList();

    public ManagerItemsObservableList(XPlaneProperty xPlaneProperty, Function<XPlane, Manager<T>> managerGetter, Function<T, U> mapper) {
        this.managerGetter = managerGetter;
        this.mapper = mapper;
        xPlaneProperty.addListener((observable, oldValue, newValue) -> reload(newValue));
        reload(xPlaneProperty.get());
    }

    private void reload(XPlane xPlane) {
        if (xPlane == null) {
            observableList.clear();
            return;
        }
        Manager<T> manager = managerGetter.apply(xPlane);
        manager.registerListener(this);
        manager.reload();
    }

    @Override
    public void onEvent(ManagerEvent<T> event) {
        log.debug("Received event: {}", event.getClass());

        if (event instanceof ManagerEvent.Loading) {
            log.debug("Manager is loading items");
            Platform.runLater(() -> {
                getLoadingProperty().set(true);
                observableList.clear();
            });

        } else if (event instanceof ManagerEvent.Loaded<T> loadedEvent) {
            log.debug("Manager has finished loading, {} items loaded", loadedEvent.getItems().size());
            Platform.runLater(() -> {
                getLoadingProperty().set(false);
                final List<T> items = loadedEvent.getItems();
                // map items to UI items
                final List<U> uiItems = items.stream().map(mapper).toList();
                observableList.setAll(uiItems);
            });

        }

    }

}
