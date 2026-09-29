package com.ogerardin.xpman.settings;

import com.ogerardin.xpman.XPmanFX;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller of the Settings dialog: hierarchic category tree on the left, settings pane on the right.
 * Panes are lazily loaded and cached; on OK, every loaded {@link SettingsPage} gets applied and the
 * config is saved.
 */
@RequiredArgsConstructor
public class SettingsController {

    private static final int ICON_SIZE = 16;

    private final XPmanFX mainController;

    @FXML
    private TreeView<SettingsCategory> categoryTree;

    @FXML
    private StackPane contentArea;

    private final Map<SettingsCategory, Node> paneCache = new EnumMap<>(SettingsCategory.class);
    private final Map<SettingsCategory, Object> controllerCache = new EnumMap<>(SettingsCategory.class);

    @FXML
    private void initialize() {
        TreeItem<SettingsCategory> root = new TreeItem<>();
        for (SettingsCategory category : SettingsCategory.values()) {
            FontIcon icon = new FontIcon(category.getIconLiteral());
            icon.setIconSize(ICON_SIZE);
            TreeItem<SettingsCategory> item = new TreeItem<>(category);
            item.setGraphic(icon);
            root.getChildren().add(item);
        }
        categoryTree.setRoot(root);
        categoryTree.getSelectionModel().selectedItemProperty().addListener((__, ___, item) ->
                Optional.ofNullable(item).map(TreeItem::getValue).ifPresent(category -> {
                    showCategory(category);
                    mainController.getConfig().setSettingsCategory(category);
                    mainController.saveConfig();
                }));
    }

    /** Selects the given category (which displays its pane). */
    public void select(SettingsCategory category) {
        categoryTree.getRoot().getChildren().stream()
                .filter(item -> item.getValue() == category)
                .findFirst()
                .ifPresent(item -> categoryTree.getSelectionModel().select(item));
    }

    private void showCategory(SettingsCategory category) {
        Node content = paneCache.computeIfAbsent(category, this::loadPane);
        if (contentArea.getChildren().isEmpty() || contentArea.getChildren().get(0) != content) {
            contentArea.getChildren().setAll(content);
        }
    }

    @SneakyThrows
    private Node loadPane(SettingsCategory category) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(category.getContentFxml()));
        loader.setControllerFactory(mainController::buildController);
        Node pane = loader.load();
        controllerCache.put(category, loader.getController());
        return pane;
    }

    @FXML
    private void ok() {
        controllerCache.values().forEach(controller -> {
            if (controller instanceof SettingsPage settingsPage) {
                settingsPage.apply();
            }
        });
        mainController.saveConfig();
        close();
    }

    @FXML
    private void cancel() {
        close();
    }

    private void close() {
        contentArea.getScene().getWindow().hide();
    }
}
