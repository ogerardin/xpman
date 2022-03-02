package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.jfx.TableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.NonNull;

import static javafx.beans.binding.Bindings.when;

/**
 * A row factory for a {@link TableView} that adds a customized context menu built by introspecting the item class and looking for specific
 * annotations.
 * @param <T> item class
 *
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
public class IntrospectingContextMenuTableRowFactory<T>
        extends IntrospectingContextMenu<T>
        implements TableRowFactory<T> {

    public IntrospectingContextMenuTableRowFactory(@NonNull Class<? extends T> itemClass, Object evaluationContextRoot) {
        super(itemClass, evaluationContextRoot);
    }

    @Override
    public TableRow<T> call(TableView<T> tableView) {
        TableRow<T> row = new TableRow<>();
        addContextMenu(row);
        return row;
    }

    /**
     * Builds a context menu based on exposed methods of the item class.
     */
    protected void addContextMenu(TableRow<T> row) {

        // build context menu
        TableView<T> tableView = row.getTableView();
        MenuItem[] menuItems = buildMenuItems(tableView);
        ContextMenu rowMenu = new ContextMenu(menuItems);

        // only display context menu for non-null items
        row.contextMenuProperty().bind(
                when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // before displaying context menu, cutomize it for actual row item
        row.setOnContextMenuRequested(event -> contexualizeMenu(rowMenu, row.getItem()));
    }

}
