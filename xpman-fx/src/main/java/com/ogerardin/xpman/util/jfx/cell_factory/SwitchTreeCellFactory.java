package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import com.ogerardin.xpman.util.jfx.ErrorDialog;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import org.controlsfx.control.ToggleSwitch;

/**
 * Factory for a {@code TreeTableCell<PluginRow, Boolean>} that displays the enabled state as an
 * interactive {@link ToggleSwitch}. Toggling it calls {@link PluginRow#setEnabled(boolean)} followed
 * by the given callback (e.g. reload); the switch is disabled for system plugins and hidden for
 * group header rows. On failure the switch reverts and an error dialog is shown.
 */
public class SwitchTreeCellFactory implements TreeTableCellFactory<PluginRow, Boolean> {

    private final Runnable onToggle;

    public SwitchTreeCellFactory(Runnable onToggle) {
        this.onToggle = onToggle;
    }

    @Override
    public TreeTableCell<PluginRow, Boolean> call(TreeTableColumn<PluginRow, Boolean> param) {
        return new TreeTableCell<>() {

            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                PluginRow row = empty || getTableRow() == null ? null : getTableRow().getItem();
                if (empty || value == null || row == null || row.isGroupHeader()) {
                    setGraphic(null);
                } else {
                    setGraphic(createToggle(row, value));
                }
            }

            /**
             * Creates a fresh switch for the row assignment; its value is set before the control is
             * in the scene graph (hence before its skin exists), so programmatic sets do not run
             * the skin's thumb-move animation — only user clicks animate.
             */
            private ToggleSwitch createToggle(PluginRow row, boolean value) {
                ToggleSwitch toggle = new ToggleSwitch();
                toggle.selectedProperty().addListener((__, old, selected) -> {
                    if (row.isEnabled() == selected) return; // programmatic set, nothing to do
                    try {
                        row.setEnabled(selected);
                    } catch (Exception e) {
                        toggle.setSelected(old); // back to the row's state; listener no-ops on it
                        ErrorDialog.showError(e, getScene() != null ? getScene().getWindow() : null);
                        return;
                    }
                    onToggle.run();
                });
                toggle.setDisable(row.getSystem());
                toggle.setSelected(value);
                return toggle;
            }
        };
    }
}
