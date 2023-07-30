package com.ogerardin.xpman.util.jfx.cell_factory;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<?, String>} that renders its Boolean content as "Yes"/(empty)
 */
public class BooleanCellFactory<S> implements TableCellFactory<S, Boolean> {
    
    @Override
    public TableCell<S, Boolean> call(TableColumn<S, Boolean> param) {
        return new TableCell<S, Boolean>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(Boolean.TRUE.equals(value) ? "Yes" : null);
                }
            }
        };
    }

}
