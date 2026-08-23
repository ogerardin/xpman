package com.ogerardin.xpman.util.jfx;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A reusable empty-state / loading-state placeholder node: a large muted icon, a message,
 * and an optional action button (e.g. "Install anything...").
 */
public class EmptyState extends VBox {

    public EmptyState(String iconLiteral, String message, Button action) {
        setAlignment(Pos.CENTER);
        setSpacing(10);
        setFillWidth(false);
        getStyleClass().add("empty-state");

        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(48);
        icon.getStyleClass().add("empty-state-icon");

        Label label = new Label(message);
        label.getStyleClass().add("empty-state-message");

        getChildren().add(icon);
        getChildren().add(label);
        if (action != null) {
            getChildren().add(action);
        }
    }

    public EmptyState(String iconLiteral, String message) {
        this(iconLiteral, message, null);
    }

    /**
     * @return a loading state with a spinner and the given message
     */
    public static Node loading(String message) {
        javafx.scene.control.ProgressIndicator spinner = new javafx.scene.control.ProgressIndicator();
        spinner.getStyleClass().add("empty-state-spinner");
        Label label = new Label(message);
        label.getStyleClass().add("empty-state-message");
        VBox box = new VBox(10, spinner, label);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("empty-state");
        return box;
    }
}
