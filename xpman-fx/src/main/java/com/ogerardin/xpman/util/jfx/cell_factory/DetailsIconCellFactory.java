package com.ogerardin.xpman.util.jfx.cell_factory;

import com.ogerardin.xplane.inspection.InspectionMessage;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Cell factory that displays an info icon (Feather.INFO) for inspection messages with details.
 * Shows the details text in a tooltip when hovering the icon.
 */
public class DetailsIconCellFactory implements Callback<TableColumn<InspectionMessage, String>, TableCell<InspectionMessage, String>> {

    private static final int ICON_SIZE = 14;

    @Override
    public TableCell<InspectionMessage, String> call(TableColumn<InspectionMessage, String> param) {
        return new TableCell<>() {
            private final FontIcon infoIcon = new FontIcon(Feather.INFO);
            private final Tooltip detailsTooltip = new Tooltip();

            {
                infoIcon.setIconSize(ICON_SIZE);
                infoIcon.setIconColor(Color.GRAY);

                detailsTooltip.setWrapText(true);
                detailsTooltip.setMaxWidth(400);
                setTooltip(detailsTooltip);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setStyle("");
                    detailsTooltip.setText(null);
                } else {
                    setGraphic(infoIcon);
                    setStyle("-fx-alignment: center;");
                    detailsTooltip.setText(item);
                }
            }
        };
    }
}
