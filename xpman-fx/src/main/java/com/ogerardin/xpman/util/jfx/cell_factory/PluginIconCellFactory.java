package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.plugins.UiPlugin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Factory for a {@code TableCell<UiPlugin, Boolean>} that displays a shield icon for system plugins.
 */
public class PluginIconCellFactory implements TableCellFactory<UiPlugin, Boolean> {

    private static final double ICON_SIZE = 24.0;

    @Override
    public TableCell<UiPlugin, Boolean> call(TableColumn<UiPlugin, Boolean> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Boolean system, boolean empty) {
                super.updateItem(system, empty);

                if (empty) {
                    setGraphic(null);
                    setTooltip(null);
                    return;
                }

                if (Boolean.TRUE.equals(system)) {
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
