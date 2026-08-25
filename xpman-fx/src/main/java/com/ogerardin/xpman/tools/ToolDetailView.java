package com.ogerardin.xpman.tools;

import com.ogerardin.xplane.tools.InstalledTool;
import com.ogerardin.xplane.tools.Manifest;
import com.ogerardin.xplane.tools.ToolIcon;
import com.ogerardin.xplane.util.platform.Platforms;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * A detail panel displaying comprehensive information about a selected tool.
 */
public class ToolDetailView extends VBox {

    private static final int LARGE_ICON_SIZE = 64;
    private static final int LARGE_ICON_FONT_SIZE = 40;

    public ToolDetailView() {
        getStyleClass().add("tool-detail");
        setSpacing(16);
        setPadding(new Insets(20));
        setAlignment(Pos.TOP_LEFT);
    }

    public void setTool(UiTool uiTool) {
        getChildren().clear();
        if (uiTool == null) {
            return;
        }
        getChildren().addAll(
                buildHeader(uiTool),
                buildDescription(uiTool),
                buildMetadata(uiTool),
                buildHomepage(uiTool)
        );
    }

    private Node buildHeader(UiTool uiTool) {
        Node icon = resolveLargeIcon(uiTool);

        Label nameLabel = new Label(uiTool.getName());
        nameLabel.getStyleClass().add("tool-detail-name");

        Label versionLabel = new Label(uiTool.getVersion() != null ? uiTool.getVersion() : "");
        versionLabel.getStyleClass().add("tool-detail-version");

        Label statusLabel = new Label(uiTool.isInstalled() ? "Installed" : "Available");
        statusLabel.getStyleClass().add("tool-detail-status");

        VBox textContent = new VBox(4, nameLabel, versionLabel, statusLabel);
        HBox header = new HBox(16, icon, textContent);
        header.getStyleClass().add("tool-detail-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private Node resolveLargeIcon(UiTool uiTool) {
        ToolIcon toolIcon = uiTool.getManifest() != null ? uiTool.getManifest().icon() : null;

        if (toolIcon instanceof ToolIcon.Url(var url)) {
            ImageView imageView = new ImageView(new Image(url.toExternalForm(), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true, true));
            imageView.setFitWidth(LARGE_ICON_SIZE);
            imageView.setFitHeight(LARGE_ICON_SIZE);
            imageView.getStyleClass().add("tool-detail-icon");
            return imageView;
        } else if (toolIcon instanceof ToolIcon.Resource(var path)) {
            var resourceUrl = getClass().getResource(path);
            if (resourceUrl != null) {
                ImageView imageView = new ImageView(new Image(resourceUrl.toExternalForm(), LARGE_ICON_SIZE, LARGE_ICON_SIZE, true, true));
                imageView.setFitWidth(LARGE_ICON_SIZE);
                imageView.setFitHeight(LARGE_ICON_SIZE);
                imageView.getStyleClass().add("tool-detail-icon");
                return imageView;
            }
        } else if (toolIcon instanceof ToolIcon.IconFont(var literal)) {
            FontIcon fontIcon = new FontIcon(literal);
            fontIcon.setIconSize(LARGE_ICON_FONT_SIZE);
            fontIcon.getStyleClass().add("tool-detail-icon");
            return fontIcon;
        }

        FontIcon defaultIcon = new FontIcon(Feather.TOOL);
        defaultIcon.setIconSize(LARGE_ICON_FONT_SIZE);
        defaultIcon.getStyleClass().add("tool-detail-icon");
        return defaultIcon;
    }

    private Node buildDescription(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        String description = manifest != null ? manifest.description() : null;
        Label label = new Label(description != null ? description : "No description available");
        label.getStyleClass().add("tool-detail-description");
        label.setWrapText(true);
        return label;
    }

    private Node buildMetadata(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        if (manifest == null) {
            return new Label();
        }

        VBox metadata = new VBox(8);
        metadata.getStyleClass().add("tool-detail-metadata");

        if (manifest.platform() != null) {
            metadata.getChildren().add(new Label("Platform: " + manifest.platform()));
        }
        if (manifest.xplaneVersion() != null) {
            metadata.getChildren().add(new Label("X-Plane version: " + manifest.xplaneVersion()));
        }
        if (uiTool.isInstalled() && uiTool.getTool() instanceof InstalledTool installedTool) {
            metadata.getChildren().add(new Label("Installed at: " + installedTool.getApp()));
        }

        return metadata;
    }

    private Node buildHomepage(UiTool uiTool) {
        Manifest manifest = uiTool.getManifest();
        if (manifest == null || manifest.homepage() == null) {
            return new Label();
        }
        Hyperlink hyperlink = new Hyperlink("Tool homepage");
        hyperlink.getStyleClass().add("tool-detail-homepage");
        hyperlink.setOnAction(__ -> Platforms.getCurrent().openUrl(manifest.homepage()));
        return hyperlink;
    }
}
