package com.ogerardin.xpman.panels;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public
class LoadTask<T> extends Task<Void> {

    private static final ImageView LOADING
            = new ImageView(new Image(LoadTask.class.getResource("/loading.gif").toExternalForm()));

    private final TableView<T> tableView;
    private final Supplier<List<T>> supplier;

    @SneakyThrows
    @Override
    protected Void call() {
        log.debug("begin load table {}", tableView.getId());
        // save placeholder to restore it later
        Node defaultPlaceholder = tableView.placeholderProperty().get();
        // replace placeholder with loading animation and clear table so it is displayed
        Platform.runLater(() -> {
            tableView.placeholderProperty().setValue(LOADING);
            tableView.setItems(null);
        });
        // call supplier to obtain items (this may take time) and load list
        List<T> list = supplier.get();
        tableView.setItems(new ObservableListWrapper<>(list));
        // reset placeholder
        Platform.runLater(() -> {
            tableView.placeholderProperty().setValue(defaultPlaceholder);
        });
        log.debug("done load table {}", tableView.getId());
        return null;
    }
}
