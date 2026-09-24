package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.scenery.UiSceneryEntry;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Renders an update-available badge icon in the scenery table when the entry has a pending update.
 */
public class SceneryUpdateCellFactory implements TableCellFactory<UiSceneryEntry, Boolean> {

    private static final int ICON_SIZE = 14;

    @Override
    public TableCell<UiSceneryEntry, Boolean> call(javafx.scene.control.TableColumn<UiSceneryEntry, Boolean> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || !item) {
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
