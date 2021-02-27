package com.ogerardin.xpman.util.jfx.panels;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

/**
 * A {@link Task} that loads a TableView from a List of items provided by a Supplier.
 * @param <T> the type of the TableView's items.
 */
@Slf4j
@RequiredArgsConstructor
public class TableViewLoadTask<T> extends Task<ObservableList<T>> {

    private static final ImageView LOADING
            = new ImageView(new Image(TableViewLoadTask.class.getResource("/loading.gif").toExternalForm()));

    private final TableView<T> tableView;
    private final Supplier<List<T>> supplier;

    @SneakyThrows
    @Override
    protected ObservableList<T> call() {
        log.debug("begin load table {}", tableView.getId());
        // save placeholder to restore it later
        Node defaultPlaceholder = tableView.placeholderProperty().get();
        // replace placeholder with loading animation and clear table so it is displayed
        Platform.runLater(() -> {
            tableView.placeholderProperty().setValue(LOADING);
            tableView.setItems(null);
        });

        // call supplier to obtain items (this may take time) and populate table
        List<T> list = supplier.get();

        // if the list is not an ObservableList, wrap it in a ObservableListWrapper
        ObservableList<T> observableList = (list instanceof ObservableList) ?
                (ObservableList<T>) list : new ObservableListWrapper<>(list);

        // populate the tableView
        Platform.runLater(() -> {
            tableView.setItems(observableList);
            // reset placeholder
            tableView.placeholderProperty().setValue(defaultPlaceholder);
        });
        log.debug("done load table {}", tableView.getId());
        return observableList;
    }
}
