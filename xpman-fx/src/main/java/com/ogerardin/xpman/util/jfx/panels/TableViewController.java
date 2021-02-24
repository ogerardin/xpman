package com.ogerardin.xpman.util.jfx.panels;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.panels.aircrafts.AircraftsController;
import com.ogerardin.xpman.panels.aircrafts.UiAircraft;
import com.ogerardin.xpman.panels.diag.DiagController;
import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.panels.menu.*;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static javafx.beans.binding.Bindings.when;

/**
 * A Controller for a {@link TableView} where the list of displayed items depend on the value of an {@link ObservableValue}.
 * Whenever the value changes, a loader is called to obtain the new list of items.
 * @param <O> type of the observed value
 * @param <T> type of the items that depend on the O value
 */
@Slf4j
public class TableViewController<O, T> {

    private final Function<O, List<T>> loader;

    /** The table to update. This property must be set in the initialize method, after FXML bindings have been populated */
    @Getter
    @Setter
    private TableView<T> tableView;

    /** Current value of the {@link ObservableValue} */
    @Getter(AccessLevel.PROTECTED)
    private O propertyValue;


    /**
     * Builds a {@link TableViewController}
     * @param observableValue the property on which the items displayed depend
     * @param loader a function that provides the list of items to display based on the observable value
     */
    public TableViewController(ObservableValue<O> observableValue, Function<O, List<T>> loader) {
        this.loader = loader;

        observableValue.addListener((observable, oldValue, newValue) -> {
            propertyValue = newValue;
            reload();
        });
    }

    public void reload() {
        TableViewLoadTask<T> loadTask = new TableViewLoadTask<>(
                tableView,
                () -> loader.apply(propertyValue)
        );

        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Builds a dynamic context menu based on exposed methods of the specified item class.
     * @see Label
     * @see EnabledIf
     * @see Confirm
     * @see ForEach
     */
    protected void setContextMenu(TableRow<T> row, Class<T> itemClass) {
        // build context menu
        ContextMenu rowMenu = new ContextMenu();
        rowMenu.getItems().addAll(
                Arrays.stream(itemClass.getDeclaredMethods())
                        // skip if method is an Object method
                        .filter(method -> {
                            try {
                                Object.class.getMethod(method.getName(), method.getParameterTypes());
                                return false;
                            } catch (Exception e) {
                                return true;
                            }
                        })
                        // skip non public or abstract methods
                        .filter(method -> Modifier.isPublic(method.getModifiers()) && ! Modifier.isAbstract(method.getModifiers()))
                        // skip setters/getters
                        .filter(method -> ! method.getName().startsWith("set") &&! method.getName().startsWith("get") && ! method.getName().startsWith("is"))
                        .map(this::buildMenuItem)
                        .toArray(MenuItem[]::new)
        );

        // only display context menu for non-null items:
        row.contextMenuProperty().bind(
                when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // cutomize menu for actual row item
        row.setOnContextMenuRequested(event -> {
            T item = getTableView().getSelectionModel().getSelectedItem();
            contexualizeMenu(rowMenu, item);
        });
    }

    private MenuItem buildMenuItem(Method method) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach == null) {
            //TODO we should be able to use @Value on methods that are not annotated with @ForEach
            var label = method.getAnnotation(com.ogerardin.xpman.util.jfx.panels.menu.Label.class);
            String text;
            if (label != null) {
                String expr = label.value();
                text = (String) SpelUtil.eval(expr, null);
            } else {
                // no @Label: try to make up something human-readable from the method name
                String[] words = StringUtils.splitByCharacterTypeCamelCase(method.getName());
                words[0] = StringUtils.capitalize(words[0]);
                text = String.join(" ", words);
            }
            return new MethodMenuItem<UiAircraft>(this, text, method, null);
        }
        return new GroupMenuItem<UiAircraft>(this, forEach, method);
    }

    private void contexualizeMenu(ContextMenu contextMenu, T item) {
        contextMenu.getItems().stream()
                .filter(Contextualizable.class::isInstance)
                .map(menuItem -> (Contextualizable<T>) menuItem)
                .forEach(contextualizable -> contextualizable.contextualize(item));

    }

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
