package com.ogerardin.xpman.util.jfx.panels;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.events.EventListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * An {@link EventListener} that handles {@link ManagerEvent}s from a {@link com.ogerardin.xplane.Manager}{@code <T>}
 * to update a given {@link TableView}{@code <U>}.
 * {@link com.ogerardin.xplane.ManagerEvent.Loading} triggers an animated placeholder.
 * {@link com.ogerardin.xplane.ManagerEvent.Loaded} triggers the display of loaded items after mapping from T to U
 * using the provided mapping function.
 *
 * @param <T> type of the items managed by the Manager
 * @param <U> type of the items displayed in the TableView
 */
@Slf4j
@Data
public class TableViewManagerEventListener<T, U> implements EventListener<ManagerEvent<T>> {

    @SuppressWarnings("ConstantConditions")
    private final ImageView LOADING = new ImageView(new Image(this.getClass().getResource("/img/loading.gif").toExternalForm()));

    private final TableView<U> tableView;
    private final Function<T, U> mapper;

    private Node defaultPlaceholder;

    public void onEvent(ManagerEvent<T> event) {
        log.debug("Received event: {}", event);

        if (event instanceof ManagerEvent.Loading) {
            log.debug("Manager is loading items");
            // save placeholder to restore it later
            defaultPlaceholder = tableView.placeholderProperty().get();
            // replace placeholder with loading animation and clear table so it is displayed
            Platform.runLater(() -> {
                tableView.placeholderProperty().setValue(LOADING);
                tableView.setItems(null);
            });

        } else if (event instanceof ManagerEvent.Loaded<T> loadedEvent) {
            log.debug("Manager has finished loading items");
            final List<T> items = loadedEvent.getItems();

            // map items to UI items
            final List<U> uiItems = items.stream().map(mapper).toList();

            // build ObservableList based on the items
            ObservableList<U> observableList = FXCollections.observableList(uiItems);
            // wrap it in SortedList to allow sorting through the UI (clicking on colmun header)
            SortedList<U> sortedList = new SortedList<>(observableList);

            // populate the tableView
            Platform.runLater(() -> {
                sortedList.comparatorProperty().bind(tableView.comparatorProperty());
                tableView.setItems(sortedList);
                // reset placeholder
                tableView.placeholderProperty().setValue(defaultPlaceholder);
            });

        }
    }
}