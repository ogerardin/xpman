package com.ogerardin.javafx.panels;

import com.ogerardin.javafx.panels.menu.*;
import com.ogerardin.xpman.panels.aircrafts.UiAircraft;
import com.ogerardin.xpman.util.SpelUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static javafx.beans.binding.Bindings.when;

/**
 * A Controller for a {@link TableView} where the list of displayed items depend on the value of an {@link ObservableValue}.
 * Whenever the value changes, a loader it called to obtain the new list of itmes
 * @param <O> type of the observed value
 * @param <T> type of the items that depend on the O value
 */
@Slf4j
@Data
public class TableViewController<O, T> {

    /** The table to update. This property must be set in the initialize method, after FXML bindings have been populated */
    private TableView<T> tableView;

    @Getter(AccessLevel.PROTECTED)
    private O propertyValue;

    public TableViewController(
            ObservableValue<O> observableValue,
            Function<O, List<T>> loader) {

        observableValue.addListener((observable, oldValue, newValue) -> {
            propertyValue = newValue;

            LoadTask<T> loadTask = new LoadTask<>(tableView, () -> loader.apply(propertyValue));

            Thread thread = new Thread(loadTask);
            thread.setDaemon(true);
            thread.start();
        });
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
                        .filter(method -> method.getReturnType() == Void.TYPE)
                        .filter(method -> ! method.getName().startsWith("set"))
                        .map(this::buildMenuItem)
                        .toArray(MenuItem[]::new)
        );

        // only display context menu for non-null items:
        row.contextMenuProperty().bind(
                when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // cutomize menu for actual row item (links, ...)
        row.setOnContextMenuRequested(event -> {
            T aircraft = getTableView().getSelectionModel().getSelectedItem();
            contexualizeMenu(rowMenu, aircraft);
        });
    }

    private MenuItem buildMenuItem(Method method) {
        ForEach forEach = method.getAnnotation(ForEach.class);
        if (forEach == null) {
            var label = method.getAnnotation(com.ogerardin.javafx.panels.menu.Label.class);
            String text;
            if (label != null) {
                String expr = label.value();
                text = (String) SpelUtil.eval(expr, null);
            } else {
                // no label: try to make up something human-readable from the method name
                String[] words = StringUtils.splitByCharacterTypeCamelCase(method.getName());
                text = String.join(" ", words);
            }
            MethodMenuItem<UiAircraft> menuItem = new MethodMenuItem<>(text, method, null);

            var refreshAfter = method.getAnnotation(RefreshAfter.class);
            if (refreshAfter != null) {
                menuItem.setAftermethodExecution(() -> getTableView().refresh());
            }
            return menuItem;
        }
        return new GroupMenuItem<UiAircraft>(forEach, method);
    }

    private void contexualizeMenu(ContextMenu contextMenu, T item) {
        contextMenu.getItems().stream()
                .filter(menuItem -> menuItem instanceof Contextualizable)
                .map(menuItem -> (Contextualizable<T>) menuItem)
                .forEach(contextualizable -> contextualizable.contextualize(item));

    }
}
