package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;

/**
 * Factory for a {@code TreeTableCell<?, String>} that renders its Boolean content as "Yes"/(empty)
 */
public class BooleanTreeCellFactory<S> implements TreeTableCellFactory<S, Boolean> {

    @Override
    public TreeTableCell<S, Boolean> call(TreeTableColumn<S, Boolean> param) {
        return new TreeTableCell<S, Boolean>() {
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
