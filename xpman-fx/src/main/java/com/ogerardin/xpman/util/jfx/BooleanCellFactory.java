package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * Factory for a {@code TableCell<String>} that renders its Boolean content as "Yes"/(empty)
 */
public class BooleanCellFactory<C> implements Callback<TableColumn<C, Boolean>, TableCell<C, Boolean>> {


    @Override
    public TableCell<C, Boolean> call(TableColumn<C, Boolean> param) {
        return new TableCell<C, Boolean>() {
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
