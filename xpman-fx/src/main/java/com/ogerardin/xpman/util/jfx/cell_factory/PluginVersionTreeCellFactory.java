package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import javafx.scene.control.ContentDisplay;
import com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil;
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
                    setGraphic(SkunkcraftsUpdateUtil.updateAction(
                            row.isBetaUpdateAvailable(), row.getUpdateTooltip(), row::skunkcraftsUpdate));
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        };
    }
}
