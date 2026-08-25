package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.ToolIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A card displaying a single tool: icon, name, description, version badge, and action button.
 */
public class ToolCardView extends HBox {

    private static final int ICON_SIZE = 32;
    private static final int ICON_FONT_SIZE = 20;

    public ToolCardView(UiTool uiTool) {
        getStyleClass().add("tool-card");
        setSpacing(12);
        setPadding(new Insets(12));
        setAlignment(Pos.CENTER_LEFT);

        Node icon = resolveIcon(uiTool);
        VBox content = buildContent(uiTool);
        Button actionButton = buildActionButton(uiTool);

        HBox.setHgrow(content, Priority.ALWAYS);
        getChildren().addAll(icon, content, actionButton);
    }

    private Node resolveIcon(UiTool uiTool) {
        ToolIcon toolIcon = uiTool.getManifest() != null ? uiTool.getManifest().icon() : null;

        if (toolIcon instanceof ToolIcon.Url(var url)) {
            ImageView imageView = new ImageView(new Image(url.toExternalForm(), ICON_SIZE, ICON_SIZE, true, true));
            imageView.setFitWidth(ICON_SIZE);
            imageView.setFitHeight(ICON_SIZE);
            imageView.getStyleClass().add("tool-card-icon");
            return imageView;
        } else if (toolIcon instanceof ToolIcon.Resource(var path)) {
            var resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                ImageView imageView = new ImageView(new Image(resourceUrl.toExternalForm(), ICON_SIZE, ICON_SIZE, true, true));
                imageView.setFitWidth(ICON_SIZE);
                imageView.setFitHeight(ICON_SIZE);
                imageView.getStyleClass().add("tool-card-icon");
                return imageView;
            }
        } else if (toolIcon instanceof ToolIcon.IconFont(var literal)) {
            FontIcon fontIcon = new FontIcon(literal);
            fontIcon.setIconSize(ICON_FONT_SIZE);
            fontIcon.getStyleClass().add("tool-card-icon");
            return fontIcon;
        }

        FontIcon defaultIcon = new FontIcon(Feather.TOOL);
        defaultIcon.setIconSize(ICON_FONT_SIZE);
        defaultIcon.getStyleClass().add("tool-card-icon");
        return defaultIcon;
    }

    private VBox buildContent(UiTool uiTool) {
        Label nameLabel = new Label(uiTool.getName());
        nameLabel.getStyleClass().add("tool-card-name");

        String description = uiTool.getManifest() != null ? uiTool.getManifest().description() : "";
        Label descriptionLabel = new Label(description != null ? description : "");
        descriptionLabel.getStyleClass().add("tool-card-description");
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        Label versionLabel = new Label(uiTool.getVersion() != null ? uiTool.getVersion() : "");
        versionLabel.getStyleClass().add("tool-card-version");

        VBox content = new VBox(4, nameLabel, descriptionLabel, versionLabel);
        content.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(content, Priority.ALWAYS);
        return content;
    }

    private Button buildActionButton(UiTool uiTool) {
        Button button = new Button();
        button.getStyleClass().add("tool-card-action");

        if (uiTool.isInstalled()) {
            button.setText("Run");
            button.setGraphic(new FontIcon(Feather.PLAY));
            button.setOnAction(__ -> uiTool.run());
        } else if (uiTool.isInstallable()) {
            button.setText("Install");
            button.setGraphic(new FontIcon(Feather.DOWNLOAD));
            button.setOnAction(__ -> uiTool.install());
        }

        return button;
    }
}
