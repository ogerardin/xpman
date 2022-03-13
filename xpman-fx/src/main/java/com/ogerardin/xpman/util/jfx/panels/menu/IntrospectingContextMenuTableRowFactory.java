package com.ogerardin.xpman.util.jfx.panels.menu;

import com.ogerardin.xpman.util.jfx.TableRowFactory;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import lombok.extern.slf4j.Slf4j;

/**
 * A row factory for a {@link TableView} that adds a customized context menu built by introspecting the item class and
 * looking for specific annotations.
 * @param <T> item class
 *
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
@Slf4j
public class IntrospectingContextMenuTableRowFactory<T>
        extends IntrospectingContextMenu<T>
        implements TableRowFactory<T> {

    public IntrospectingContextMenuTableRowFactory(Object evaluationContextRoot) {
        super(evaluationContextRoot);
    }

    public TableRow<T> call(TableView<T> treeTableView) {
        TableRow<T> row = new TableRow<>();

        row.itemProperty().addListener((observable, oldItem, newItem) -> {
            if (newItem == null) {
                row.setContextMenu(null);
                return;
            }
            log.debug("Loading menu for {}", newItem);
            ContextMenu rowMenu = getContextMenu(newItem);
            row.setContextMenu(rowMenu);
        });

        return row;
    }
}
