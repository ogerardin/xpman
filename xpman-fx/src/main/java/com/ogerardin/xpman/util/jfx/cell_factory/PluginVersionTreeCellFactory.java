package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

/**
 * Renders a plugin version with an update-available pill when needed.
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

                PluginRow row = getTreeTableRow() == null ? null : getTreeTableRow().getItem();
                if (row != null && row.isUpdateAvailable()) {
                    Label pill = new Label("Update available");
                    pill.getStyleClass().add("pill-warning");
                    String latestVersion = row.getLatestVersion();
                    if (latestVersion != null) pill.setTooltip(new Tooltip("Latest version: " + latestVersion));
                    setGraphic(pill);
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        };
    }
}
