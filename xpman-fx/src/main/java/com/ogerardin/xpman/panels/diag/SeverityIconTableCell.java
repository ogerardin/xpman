package com.ogerardin.xpman.panels.diag;

import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.Maps;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Map;

public class SeverityIconTableCell<S> extends TableCell<S, Severity> {

    private static final Image ICON_ERROR = new Image("/com/sun/javafx/scene/control/skin/modena/dialog-error.png");
    private static final Image ICON_WARN = new Image("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png");
    private static final Image ICON_INFO = new Image("/com/sun/javafx/scene/control/skin/modena/dialog-information.png");

    private static final Map<Severity, Image> SEVERITY_IMAGE_MAP = Maps.mapOf(
            Severity.ERROR, ICON_ERROR,
            Severity.WARN, ICON_WARN,
            Severity.INFO, ICON_INFO
            );

    private final ImageView imageView = new ImageView();

    public SeverityIconTableCell() {
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(24.0);
        setGraphic(imageView);
    }

    @Override
    protected void updateItem(Severity item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            imageView.setImage(null);
        } else {
            Image icon = getSeverityIcon(item);
            imageView.setImage(icon);
        }
    }

    private Image getSeverityIcon(Severity severity) {
        return SEVERITY_IMAGE_MAP.get(severity);
    }
}
