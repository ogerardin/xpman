package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xpman.util.jfx.cell_factory.TableCellFactory;
import javafx.beans.NamedArg;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Factory for a {@code TableCell<?, Severity>} where the {@link Severity} is represented by a themed icon
 * (error = danger octagon, warning = alert triangle, info = info circle) colored via style classes.
 */
@Slf4j
public class SeverityIconCellFactory<T> implements TableCellFactory<T, Severity> {

    private final boolean showText;

    public SeverityIconCellFactory(@NamedArg("showText") boolean showText) {
        this.showText = showText;
    }

    @Override
    public TableCell<T, Severity> call(TableColumn<T, Severity> param) {
        return new SeverityIconTableCell<>();
    }

    private static FontIcon getSeverityIcon(Severity severity) {
        Feather icon = switch (severity) {
            case ERROR -> Feather.ALERT_OCTAGON;
            case WARN -> Feather.ALERT_TRIANGLE;
            case INFO -> Feather.INFO;
        };
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.getStyleClass().add("severity-icon-" + severity.name().toLowerCase());
        fontIcon.setIconSize(16);
        return fontIcon;
    }

    private class SeverityIconTableCell<S> extends TableCell<S, Severity> {

        public SeverityIconTableCell() {
        }

        @Override
        protected void updateItem(Severity item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(getSeverityIcon(item));
                if (showText) {
                    setText(item.toString());
                }
            }
        }
    }
}
