package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<S, SceneryEntryStatus>} that renders IN_INI as "Yes",
 * IN_INI_DISABLED as "No", and any other status as empty.
 */
public class SceneryStatusCellFactory<S> implements TableCellFactory<S, SceneryEntryStatus> {

    @Override
    public TableCell<S, SceneryEntryStatus> call(TableColumn<S, SceneryEntryStatus> param) {
        return new TableCell<S, SceneryEntryStatus>() {
            @Override
            protected void updateItem(SceneryEntryStatus value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(switch (value) {
                        case IN_INI -> "Yes";
                        case IN_INI_DISABLED -> "No";
                        default -> null;
                    });
                }
            }
        };
    }

}
