package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.util.jfx.ErrorDialog;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import org.controlsfx.control.ToggleSwitch;

/**
 * Factory for a {@code TreeTableCell<S, Boolean>} that displays the enabled state as an interactive
 * {@link ToggleSwitch}. Toggling it calls {@link SwitchRow#setEnabled(boolean)} followed by the
 * given callback (e.g. reload); the switch is locked for rows where {@link SwitchRow#isSwitchLocked()}
 * is true and hidden for rows where {@link SwitchRow#isSwitchHidden()} is true. On failure the switch
 * reverts and an error dialog is shown.
 */
public class SwitchTreeCellFactory<S extends SwitchRow> implements TreeTableCellFactory<S, Boolean> {

    private final Runnable onToggle;

    public SwitchTreeCellFactory(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    @Override
    public TreeTableCell<S, Boolean> call(TreeTableColumn<S, Boolean> param) {
        return new SwitchTreeTableCell();
    }

    private class SwitchTreeTableCell extends TreeTableCell<S, Boolean> {

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

        /** Sets the initial value before the skin exists, so only user clicks animate. */
        private ToggleSwitch createToggle(S row, boolean value) {
            ToggleSwitch toggle = new ToggleSwitch();
            toggle.selectedProperty().addListener((_, old, selected) -> {
                if (row.isEnabled() == selected.booleanValue()) return; // programmatic set, nothing to do
                try {
                    row.setEnabled(selected);
                } catch (Exception e) {
                    toggle.setSelected(old); // back to the row's state; listener no-ops on it
                    ErrorDialog.showError(e, getScene() != null ? getScene().getWindow() : null);
                    return;
                }
                onToggle.run();
            });
            toggle.setDisable(row.isSwitchLocked());
            toggle.setSelected(value);
            return toggle;
        }
    }
}
