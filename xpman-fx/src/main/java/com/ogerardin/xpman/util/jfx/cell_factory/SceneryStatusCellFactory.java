package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<S, SceneryEntryStatus>} that renders the status label
 * (empty for statuses without a label).
 */
public class SceneryStatusCellFactory<S> implements TableCellFactory<S, SceneryEntryStatus> {

    @Override
    public TableCell<S, SceneryEntryStatus> call(TableColumn<S, SceneryEntryStatus> param) {
        return new TableCell<S, SceneryEntryStatus>() {
            @Override
            protected void updateItem(SceneryEntryStatus value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : value.getLabel());
            }
        };
    }

}
