package com.ogerardin.xpman.util.jfx.menu;

import com.ogerardin.xpman.util.jfx.TreeTableRowFactory;
import com.ogerardin.xpman.util.jfx.menu.annotation.Confirm;
import com.ogerardin.xpman.util.jfx.menu.annotation.EnabledIf;
import com.ogerardin.xpman.util.jfx.menu.annotation.ForEach;
import com.ogerardin.xpman.util.jfx.menu.annotation.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import lombok.extern.slf4j.Slf4j;

/**
 * A row factory for a {@link TreeTableView} that adds a customized context menu built by introspecting the item class
 * and looking for specific annotations.
 *
 * @param <T> item class
 * @see Label
 * @see EnabledIf
 * @see Confirm
 * @see ForEach
 */
@Slf4j
public class IntrospectingContextMenuTreeTableRowFactory<T> extends IntrospectingContextMenuFactory<T>
        implements TreeTableRowFactory<T> {

    public IntrospectingContextMenuTreeTableRowFactory(Object evaluationContextRoot) {
        super(evaluationContextRoot);
    }

    public TreeTableRow<T> call(TreeTableView<T> treeTableView) {
        TreeTableRow<T> row = new TreeTableRow<>();

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
