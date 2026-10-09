package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Renders an update-available badge icon in the plugins tree table when the plugin has a pending update.
 */
public class PluginUpdateTreeCellFactory implements Callback<TreeTableColumn<PluginRow, Boolean>, TreeTableCell<PluginRow, Boolean>> {

    private static final int ICON_SIZE = 14;

    @Override
    public TreeTableCell<PluginRow, Boolean> call(TreeTableColumn<PluginRow, Boolean> param) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || !item.booleanValue()) {
                    setGraphic(null);
                    setTooltip(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    return;
                }
                FontIcon icon = new FontIcon(Feather.ARROW_UP_CIRCLE);
                icon.setIconSize(ICON_SIZE);
                icon.getStyleClass().add("update-badge-icon");
                setGraphic(icon);
                setTooltip(new Tooltip("Update available"));
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        };
    }
}
