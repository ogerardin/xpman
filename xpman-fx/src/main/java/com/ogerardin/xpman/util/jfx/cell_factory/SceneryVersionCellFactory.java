package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xpman.panels.scenery.UiSceneryEntry;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil;

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
                    setGraphic(SkunkcraftsUpdateUtil.updateAction(
                            row.getSceneryPackage().isSkunkcraftsBetaUpdateAvailable(),
                            SkunkcraftsUpdateUtil.updateTooltip(row.getSceneryPackage()), () -> {
                        new com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard(row.getSceneryPackage()).showAndWait();
                        row.getXPlane().getSceneryManager().reload();
                    }));
                    setContentDisplay(ContentDisplay.RIGHT);
                }
            }
        };
    }
}
