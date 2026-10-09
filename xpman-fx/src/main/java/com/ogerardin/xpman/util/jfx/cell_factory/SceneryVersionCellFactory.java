package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.scenery.UiSceneryEntry;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

/**
 * Renders a scenery version with an update-available pill when needed.
 */
public class SceneryVersionCellFactory implements TableCellFactory<UiSceneryEntry, String> {

    @Override
    public TableCell<UiSceneryEntry, String> call(TableColumn<UiSceneryEntry, String> param) {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                if (empty) return;

                UiSceneryEntry row = getTableRow() == null ? null : getTableRow().getItem();
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
