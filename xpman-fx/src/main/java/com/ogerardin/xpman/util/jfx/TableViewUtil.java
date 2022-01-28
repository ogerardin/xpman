package com.ogerardin.xpman.util.jfx;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.TableColumnHeader;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class TableViewUtil {

    public <S, T> void setColumnHeaderTooltip(TableView<S> tableView, TableColumn<S, T> tableColumn, String text) {
        // From https://stackoverflow.com/questions/23224826/how-to-add-a-tooltip-to-a-tableview-header-cell-in-javafx-8
        // CAVEAT: Relies on JavaFX internals and may break anytime...
        Platform.runLater(() -> {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(200);

            // Lookup column header and set tooltip
            String headerSelector = "#" + tableColumn.getId();
            TableColumnHeader header = (TableColumnHeader) tableView.lookup(headerSelector);
            if (header == null) {
                log.warn("Failed to set header tooltip: no column header matches selector " + headerSelector);
                return;
            }
            String labelSelector = ".label";
            Label label = (Label) header.lookup(labelSelector);
            if (label == null) {
                log.warn("Failed to set header tooltip: no label matches selector " + labelSelector);
                return;
            }
            label.setTooltip(tooltip);

            // Makes the tooltip display, no matter where the mouse is inside the column header.
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        });
    }
}
