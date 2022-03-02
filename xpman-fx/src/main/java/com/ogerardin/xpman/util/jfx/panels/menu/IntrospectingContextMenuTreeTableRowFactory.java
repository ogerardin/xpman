package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.jfx.TreeTableRowFactory;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import lombok.NonNull;

import static javafx.beans.binding.Bindings.when;

/**
 * A row factory for a {@link TreeTableView} that adds a customized context menu built by introspecting the item class and looking for specific
 * annotations.
 * @param <T> item class
 *
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
public class IntrospectingContextMenuTreeTableRowFactory<T>
        extends IntrospectingContextMenu<T>
        implements TreeTableRowFactory<T> {

    public IntrospectingContextMenuTreeTableRowFactory(@NonNull Class<? extends T> itemClass, Object evaluationContextRoot) {
        super(itemClass, evaluationContextRoot);
    }

    @Override
    public TreeTableRow<T> call(TreeTableView<T> tableView) {
        TreeTableRow<T> row = new TreeTableRow<>();
        addContextMenu(row);
        return row;
    }

    /**
     * Builds a context menu based on exposed methods of the item class.
     */
    protected void addContextMenu(TreeTableRow<T> row) {

        // build context menu
        TreeTableView<T> tableView = row.getTreeTableView();
        MenuItem[] menuItems = buildMenuItems(tableView);
        ContextMenu rowMenu = new ContextMenu(menuItems);

        // only display context menu for non-null items
        row.contextMenuProperty().bind(
                when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

        // before displaying context menu, cutomize it for actual row item
        row.setOnContextMenuRequested(event -> {
            contexualizeMenu(rowMenu, row.getItem());
        });
    }

}
