package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * Renders a plugin version with an "Update" button when an update is available.
 */
public class PluginVersionTreeCellFactory implements Callback<TreeTableColumn<PluginRow, String>, TreeTableCell<PluginRow, String>> {

    @Override
    public TreeTableCell<PluginRow, String> call(TreeTableColumn<PluginRow, String> param) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                if (empty) return;

                PluginRow row = getTableRow() == null ? null : getTableRow().getItem();
                if (row != null && row.isUpdateAvailable()) {
                    Button button = new Button("Update");
                    button.getStyleClass().add("updates-item-button");
                    String latestVersion = row.getLatestVersion();
                    if (latestVersion != null) button.setTooltip(new Tooltip("Update to " + latestVersion));
                    button.setOnAction(_ -> row.skunkcraftsUpdate());
                    setGraphic(button);
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        };
    }
}
