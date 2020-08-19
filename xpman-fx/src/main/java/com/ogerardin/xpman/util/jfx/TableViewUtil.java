package com.ogerardin.xpman.util.jfx;

import com.sun.javafx.scene.control.skin.TableColumnHeader;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TableViewUtil {

    public <S, T> void setColumnHeaderTooltip(TableView<S> tableView, TableColumn<S, T> tableColumn, String text) {
        // From https://stackoverflow.com/questions/23224826/how-to-add-a-tooltip-to-a-tableview-header-cell-in-javafx-8
        // Relies on JavaFX internals and my break anytime...
        Platform.runLater(() -> {
            Tooltip tooltip = new Tooltip(text);
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(200);

            // Lookup column header and set tooltip
            TableColumnHeader header = (TableColumnHeader) tableView.lookup("#" + tableColumn.getId());
            Label label = (Label) header.lookup(".label");
            label.setTooltip(tooltip);

            // Makes the tooltip display, no matter where the mouse is inside the column header.
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        });
    }
}
