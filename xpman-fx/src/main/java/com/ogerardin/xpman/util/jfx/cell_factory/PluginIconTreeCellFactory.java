package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.PluginRow;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Factory for a {@code TreeTableCell<PluginRow, Boolean>} that displays icons for system plugins and scripts.
 */
public class PluginIconTreeCellFactory implements TreeTableCellFactory<PluginRow, Boolean> {

    private static final double ICON_SIZE = 24.0;

    @Override
    public TreeTableCell<PluginRow, Boolean> call(TreeTableColumn<PluginRow, Boolean> param) {
        return new TreeTableCell<>() {
            @Override
            protected void updateItem(Boolean system, boolean empty) {
                super.updateItem(system, empty);

                if (empty) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }

                PluginRow row = getTableRow() != null ? getTableRow().getItem() : null;
                
                if (row != null && row.isScript()) {
                    FontIcon icon = new FontIcon(Feather.CODE);
                    icon.setIconSize((int) ICON_SIZE);
                    setGraphic(icon);
                    setTooltip(new Tooltip("FlyWithLuaPlugin script"));
                } else if (Boolean.TRUE.equals(system)) {
                    FontIcon icon = new FontIcon(Feather.SHIELD);
                    icon.setIconSize((int) ICON_SIZE);
                    setGraphic(icon);
                    setTooltip(new Tooltip("System plugin"));
                } else {
                    setGraphic(null);
                    setTooltip(null);
                }
            }
        };
    }
}
