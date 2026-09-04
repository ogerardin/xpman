package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xplane.util.platform.Platforms;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Optional;

/**
 * Native (WebKit-free) info dialog for a nav data item: description (HTML stripped),
 * folder, header metadata, and a link to the X-Plane nav data documentation.
 * Replaces the old WebViewStage HTML popup.
 */
public class NavDataInfoDialog extends Dialog<Void> {

    private static final String DOC_URL = "https://developer.x-plane.com/article/navdata/";

    public NavDataInfoDialog(UiNavDataItem item, Window owner) {
        initOwner(owner);
        setTitle("Nav data: " + item.getName());

        VBox content = new VBox(8);
        content.setPadding(new Insets(12));
        content.setPrefWidth(420);

        Label description = new Label(stripHtml(item.getDescription()));
        description.setWrapText(true);
        description.getStyleClass().add("navdata-info-description");
        content.getChildren().add(description);

        Optional.ofNullable(item.getPath()).ifPresent(path -> {
            Label folder = new Label(path.toString());
            folder.setWrapText(true);
            folder.getStyleClass().add("navdata-info-path");
            content.getChildren().add(folder);
        });

        VBox details = new VBox(2);
        addDetail(details, "AIRAC cycle", item.getAiracCycle());
        addDetail(details, "Metadata", item.getMetadata());
        addDetail(details, "Build", item.getBuild());
        if (!details.getChildren().isEmpty()) {
            content.getChildren().add(details);
        }

        Hyperlink docLink = new Hyperlink("X-Plane nav data documentation");
        docLink.setOnAction(__ -> openDoc());
        content.getChildren().add(docLink);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
    }

    public static void show(UiNavDataItem item, Node card) {
        Window owner = card.getScene() != null ? card.getScene().getWindow() : null;
        new NavDataInfoDialog(item, owner).showAndWait();
    }

    @SneakyThrows
    private static void openDoc() {
        Platforms.getCurrent().openUrl(new URL(DOC_URL));
    }

    private static void addDetail(VBox details, String label, String value) {
        if (value != null && !value.isBlank()) {
            details.getChildren().add(new Label(label + ": " + value));
        }
    }

    private static String stripHtml(String html) {
        String stripped = html == null ? "" : html
                .replaceAll("<[^>]*>", " ")
                .replaceAll("&#8211;", "–")
                .replaceAll("&#8220;", "“")
                .replaceAll("&#8221;", "”")
                .replaceAll("\\s+", " ")
                .trim();
        return stripped.isBlank() ? "No description available." : stripped;
    }
}
