package com.ogerardin.xpman.util.jfx.panels;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A {@link Task} that loads a TreeTableView from a root node provided by a supplier and a function that maps
 * a node of type T to a {@code TreeItem<T>}
 * @param <T> the type of the TreeTableView's items.
 */
@Slf4j
@RequiredArgsConstructor
public class TreeTableViewLoadTask<T> extends Task<Void> {

    private static final ImageView LOADING
            = new ImageView(new Image(TreeTableViewLoadTask.class.getResource("/img/loading.gif").toExternalForm()));

    private final TreeTableView<T> treeTableView;

    /**
     * Supplier for the root object of the tree
     */
    private final Supplier<T> supplier;

    /**
     * A function that converts an object of type T into a {@link TreeItem}
     */
    private final Function<T, TreeItem<T>> treeItemBuilder;

    @Override
    protected Void call() {
        log.debug("begin load treeTable {}", treeTableView.getId());
        // save placeholder to restore it later
        Node defaultPlaceholder = treeTableView.placeholderProperty().get();
        // replace placeholder with loading animation and clear table so it is displayed
        Platform.runLater(() -> {
            treeTableView.placeholderProperty().setValue(LOADING);
            treeTableView.setRoot(null);
        });

        // call supplier to get root object
        final T rootValue = supplier.get();
        // convert root object to TreeItem
        TreeItem<T> root = treeItemBuilder.apply(rootValue);
        root.expandedProperty().set(true);

        Platform.runLater(() -> {
            treeTableView.setRoot(root);
            // reset placeholder
            treeTableView.placeholderProperty().setValue(defaultPlaceholder);
        });
        log.debug("done load treeTable {}", treeTableView.getId());

        return null;
    }

}
