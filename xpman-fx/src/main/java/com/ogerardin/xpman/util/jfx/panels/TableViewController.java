package com.ogerardin.xpman.util.jfx.panels;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.panels.aircrafts.AircraftsController;
import com.ogerardin.xpman.panels.diag.DiagController;
import com.ogerardin.xpman.util.jfx.panels.menu.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * A Controller for a {@link TableView} where the list of items of obtained by invoking a loader.
 *
 * @param <T> type of the table items
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
@Slf4j
public class TableViewController<T> {

    private final Supplier<List<T>> loader;

    /**
     * The table to update. This property must be set in the initialize method, after FXML bindings have been populated
     */
    @Getter
    @Setter
    private TableView<T> tableView;

    /**
     * Builds a {@link TableViewController}
     *
     * @param observableValue the property on which the items displayed depend
     * @param loader          a function that provides the list of items to display based on the observable value
     */
    public TableViewController(Supplier<List<T>> loader) {
        this.loader = loader;
    }

    public void reload() {
        TableViewLoadTask<T> loadTask = new TableViewLoadTask<>(tableView, loader);
        Executors.newSingleThreadExecutor().submit(loadTask);
    }

    //TODO move this somewhere else
    @SuppressWarnings("unused")
    @SneakyThrows
    public void displayCheckResults(List<InspectionMessage> results) {
        FXMLLoader loader = new FXMLLoader(AircraftsController.class.getResource("/fxml/diag.fxml"));
        Pane pane = loader.load();
        DiagController controller = loader.getController();
        controller.setItems(results);
        Stage stage = new Stage();
        stage.setTitle("Analysis results");
        stage.setScene(new Scene(pane));
        stage.initOwner(this.tableView.getScene().getWindow());
        stage.show();
    }
}
