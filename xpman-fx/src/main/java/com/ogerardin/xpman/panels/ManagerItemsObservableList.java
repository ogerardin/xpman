package com.ogerardin.xpman.panels;

import com.ogerardin.xplane.Manager;
import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.events.EventListener;
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
import java.util.Optional;
import java.util.function.Function;

/**
 * This class provides an {@link ObservableList} whose items are loaded from a backing {@link Manager}
 * and mapped using a mapping funtion {@link #mapper}.
 * It is intended to be used as a value for the "items" property of a {@link javafx.scene.control.TableView}.
 * It also exposes property {@link #loadingProperty} that can be used for example to display an animation
 * while the Manager is loading items.
 * Note that the manager is not specified directly but obtained from an {@link XPlane} instance using function
 * {@link #managerGetter}, which allows automatic reloading in case of XPlane instance change.
 *
 * @param <T> the type of items handled by the Manager
 * @param <U> the type of items contained in the list.
 */
@Slf4j
public class ManagerItemsObservableList<T, U>
        implements EventListener<ManagerEvent<T>>, ObservableList<U> {

    /** function to obtain the desired manager from an XPlane value */
    private final Function<XPlane, Manager<T>> managerGetter;

    /** Function to map an item from the Manager to an item in the ObservableList */
    private final Function<T, U> mapper;

    @Getter
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty();

    @Delegate
    private final ObservableList<U> observableList = FXCollections.observableArrayList();

    private Manager<T> manager;

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
        manager = managerGetter.apply(xPlane);
        manager.registerListener(this);
        manager.reload();
    }

    @Override
    public void onEvent(ManagerEvent<T> event) {
        log.debug("Received event: {}", event);

        switch (event.getType()) {
            case LOADING -> {
                Platform.runLater(() -> {
                    getLoadingProperty().set(true);
                    observableList.clear();
                });
            }
            case LOADED -> {
                Platform.runLater(() -> {
                    getLoadingProperty().set(false);
                    final List<T> items = event.getItems();
                    // map items to UI items
                    final List<U> uiItems = items.stream().map(mapper).toList();
                    observableList.setAll(uiItems);
                });
            }
        }

    }

    public void reload() {
        Optional.ofNullable(manager).ifPresent(Manager::reload);
    }
}
