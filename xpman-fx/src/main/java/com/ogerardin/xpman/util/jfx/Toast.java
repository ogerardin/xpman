package com.ogerardin.xpman.util.jfx;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Lightweight transient toast notification shown at the bottom-right of the main window and
 * auto-dismissed after a few seconds. Use for success feedback instead of modal alerts.
 */
public final class Toast {

    private static final Duration DEFAULT_DURATION = Duration.seconds(4);

    private Toast() {
    }

    /**
     * Shows a success toast with a check-circle icon and the given message.
     */
    public static void success(Window owner, String message) {
        show(owner, Feather.CHECK_CIRCLE, "toast-success", message);
    }

    /**
     * Shows an info toast with an info icon and the given message.
     */
    public static void info(Window owner, String message) {
        show(owner, Feather.INFO, "toast-info", message);
    }

    private static void show(Window owner, Feather icon, String styleClass, String message) {
        if (owner == null || !owner.isShowing()) {
            return;
        }

        FontIcon fontIcon = new FontIcon(icon);
        fontIcon.setIconSize(16);
        Label label = new Label(message, fontIcon);
        label.getStyleClass().addAll("toast", styleClass);

        StackPane root = new StackPane(label);
        root.getStyleClass().add("toast-root");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Toast.class.getResource("/css/xpman.css").toExternalForm());
        root.applyCss();
        root.layout();

        Popup popup = new Popup();
        popup.getContent().add(root);
        popup.setAutoFix(false);

        double margin = 16;
        double x = owner.getX() + owner.getWidth() - root.getWidth() - margin;
        double y = owner.getY() + owner.getHeight() - root.getHeight() - margin;
        popup.show(owner, x, y);

        PauseTransition pause = new PauseTransition(DEFAULT_DURATION);
        pause.setOnFinished(__ -> popup.hide());
        pause.play();

        root.setOnMouseClicked(__ -> popup.hide());
    }
}
