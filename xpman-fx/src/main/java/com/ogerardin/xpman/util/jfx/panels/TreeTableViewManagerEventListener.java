package com.ogerardin.xpman.util.jfx.panels;

import com.ogerardin.xplane.ManagerEvent;
import com.ogerardin.xplane.events.EventListener;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Function;

/**
 * An {@link EventListener} that handles {@link ManagerEvent}s from a {@link com.ogerardin.xplane.Manager}{@code <T>}
 * to update a given {@link javafx.scene.control.TreeTableView}{@code <U>}.
 * {@link ManagerEvent.Loading} triggers an animated placeholder.
 * {@link ManagerEvent.Loaded} triggers the display of loaded items after mapping from List{@code <T>} to TreeItem{@code <U>}
 * using the provided mapping function.
 *
 * @param <T> type of the items managed by the Manager
 * @param <U> type of the items displayed in the TreeTableView
 */
@Slf4j
@Data
public class TreeTableViewManagerEventListener<T, U> implements EventListener<ManagerEvent<T>> {

    @SuppressWarnings("ConstantConditions")
    private final ImageView LOADING = new ImageView(new Image(this.getClass().getResource("/img/loading.gif").toExternalForm()));

    private final TreeTableView<U> treeTableView;
    private final Function<List<T>, TreeItem<U>> mapper;

    private Node defaultPlaceholder;

    public void onEvent(ManagerEvent<T> event) {
        log.debug("Received event: {}", event);

        if (event instanceof ManagerEvent.Loading) {
            log.debug("Manager is loading items");
            // save placeholder to restore it later
            defaultPlaceholder = treeTableView.placeholderProperty().get();
            // replace placeholder with loading animation and clear table so it is displayed
            Platform.runLater(() -> {
                treeTableView.placeholderProperty().setValue(LOADING);
                treeTableView.setRoot(null);
            });

        } else if (event instanceof ManagerEvent.Loaded) {
            log.debug("Manager has finished loading items");
            final List<T> items = ((ManagerEvent.Loaded<T>) event).getItems();

            // map items to UI items
            final TreeItem<U> root = mapper.apply(items);

            // populate the treetableView
            Platform.runLater(() -> {
                treeTableView.setRoot(root);
                root.setExpanded(true);
                // reset placeholder
                treeTableView.placeholderProperty().setValue(defaultPlaceholder);
            });

        }
    }
}