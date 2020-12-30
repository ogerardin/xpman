package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * Factory for a {@code TreeTableCell<String>} that renders its Boolean content as "Yes"/(empty)
 */
public class BooleanTreeCellFactory<C> implements Callback<TreeTableColumn<C, Boolean>, TreeTableCell<C, Boolean>> {


    @Override
    public TreeTableCell<C, Boolean> call(TreeTableColumn<C, Boolean> param) {
        return new TreeTableCell<C, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(value ? "Yes" : null);
                }
            }
        };
    }

}
