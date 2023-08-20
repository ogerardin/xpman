package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xpman.util.jfx.cell_factory.TableCellFactory;
import javafx.beans.NamedArg;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Factory for a {@code TableCell<?, Severity>} where the {@link Severity} is represented by an icon.
 */
@Slf4j
public class SeverityIconCellFactory<T> implements TableCellFactory<T, Severity> {

    private static final Image ICON_ERROR = getImage("/img/dialog-error.png");
    private static final Image ICON_WARN = getImage("/img/dialog-warning.png");
    private static final Image ICON_INFO = getImage("/img/dialog-information.png");

    private static final Map<Severity, Image> SEVERITY_IMAGE_MAP = Maps.mapOf(
            Severity.ERROR, ICON_ERROR,
            Severity.WARN, ICON_WARN,
            Severity.INFO, ICON_INFO
    );

    private final boolean showText;

    public SeverityIconCellFactory(@NamedArg("showText") boolean showText) {
        this.showText = showText;
    }

    @Override
    public TableCell<T, Severity> call(TableColumn<T, Severity> param) {
        return new SeverityIconTableCell<>();
    }

    private static Image getImage(String name) {
        return new Image(SeverityIconCellFactory.class.getResourceAsStream(name));
    }


    private class SeverityIconTableCell<S> extends TableCell<S, Severity> {

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
                setText(null);
            } else {
                Image icon = getSeverityIcon(item);
                imageView.setImage(icon);
                if (showText) {
                    setText(item.toString());
                }
            }
        }

        private Image getSeverityIcon(Severity severity) {
            return SEVERITY_IMAGE_MAP.get(severity);
        }
    }
}
