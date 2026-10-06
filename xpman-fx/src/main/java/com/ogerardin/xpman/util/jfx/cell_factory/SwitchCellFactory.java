package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.util.jfx.ErrorDialog;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.ToggleSwitch;

/**
 * Plain-table counterpart of {@link SwitchTreeCellFactory}: factory for a {@code TableCell<S, Boolean>}
 * that displays the enabled state as an interactive {@link ToggleSwitch}. Toggling it calls
 * {@link SwitchRow#setEnabled(boolean)} followed by the given callback (e.g. refresh); the switch is
 * locked for rows where {@link SwitchRow#isSwitchLocked()} is true and hidden for rows where
 * {@link SwitchRow#isSwitchHidden()} is true. On failure the switch reverts and an error dialog is shown.
 */
public class SwitchCellFactory<S extends SwitchRow> implements TableCellFactory<S, Boolean> {

    private final Runnable onToggle;

    public SwitchCellFactory(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    @Override
    public TableCell<S, Boolean> call(TableColumn<S, Boolean> param) {
        return new TableCell<>() {

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                S row = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (empty || value == null || row == null || row.isSwitchHidden()) {
                    setGraphic(null);
                } else {
                    setGraphic(createToggle(row, value));
                }
            }

            private ToggleSwitch createToggle(S row, boolean value) {
                ToggleSwitch toggle = new ToggleSwitch();
                toggle.selectedProperty().addListener((__, old, selected) -> {
                    if (row.isEnabled() == selected) return; // programmatic set, nothing to do
                    try {
                        row.setEnabled(selected);
                    } catch (Exception e) {
                        toggle.setSelected(old);
                        ErrorDialog.showError(e, getScene() != null ? getScene().getWindow() : null);
                        return;
                    }
                    onToggle.run();
                });
                toggle.setDisable(row.isSwitchLocked());
                toggle.setSelected(value);
                return toggle;
            }
        };
    }
}
