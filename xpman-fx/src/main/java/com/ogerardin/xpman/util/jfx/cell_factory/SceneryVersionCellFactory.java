package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.scenery.UiSceneryEntry;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

/**
 * Renders a scenery version with an "Update" button when an update is available.
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
                    Button button = new Button("Update");
                    button.getStyleClass().add("updates-item-button");
                    button.setTooltip(new Tooltip(
                            com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil.updateTooltip(row.getSceneryPackage())));
                    button.setOnAction(_ -> {
                        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(row.getSceneryPackage()).showAndWait();
                        row.getXPlane().getSceneryManager().reload();
                    });
                    setGraphic(button);
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        };
    }
}
