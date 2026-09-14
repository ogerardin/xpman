package com.ogerardin.xpman.util.jfx.wizard;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collection;
import java.util.Collections;

/**
 * A {@link GraphicValidationDecoration} that renders themed validation feedback: Ikonli feather
 * severity icons (colored via the severity icon style classes) instead of the stock ControlsFX
 * PNG images, and a CSS-styled tooltip instead of ControlsFX's hardcoded light-mode inline styles.
 */
public class ThemedValidationDecoration extends GraphicValidationDecoration {

    @Override
    protected Collection<Decoration> createRequiredDecorations(Control target) {
        // don't decorate required fields (the stock decoration makes them look invalid)
        return Collections.emptyList();
    }

    @Override
    protected Node createErrorNode() {
        return severityIcon(Feather.X_CIRCLE, com.ogerardin.xplane.inspection.Severity.ERROR);
    }

    @Override
    protected Node createWarningNode() {
        return severityIcon(Feather.ALERT_TRIANGLE, com.ogerardin.xplane.inspection.Severity.WARN);
    }

    @Override
    protected Tooltip createTooltip(ValidationMessage message) {
        Tooltip tooltip = new Tooltip(message.getText());
        tooltip.getStyleClass().add("validation-tooltip");
        return tooltip;
    }

    private static Node severityIcon(Feather icon, com.ogerardin.xplane.inspection.Severity severity) {
        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        String styleClass = switch (severity) {
            case ERROR -> "severity-icon-error";
            case WARN -> "severity-icon-warn";
            case INFO -> "severity-icon-info";
        };
        fontIcon.getStyleClass().add(styleClass);
        return fontIcon;
    }
}
