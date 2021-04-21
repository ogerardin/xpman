package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<?, ?>} that renders the line number of the row
 */
public class LineNumberCellFactory<S> implements TableCellFactory<S, Void> {
    
    @Override
    public TableCell<S, Void> call(TableColumn<S, Void> param) {
        return new TableCell<S, Void>() {
            @Override
            protected void updateItem(Void value, boolean empty) {
                setText(empty ? "" : String.valueOf(getIndex() + 1));
            }
        };
    }

}
