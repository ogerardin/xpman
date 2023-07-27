package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xpman.util.jfx.TreeTableCellFactory;
import com.ogerardin.xpman.util.jfx.WebViewStage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** Specialized {@link TreeTableCellFactory} for {@link UiNavDataItem}.
 * Items that have a description are displayed with an info icon that opens a popup with the description.
 */
@Slf4j
public class NavDataTableTreeItemCellFactory<S> implements TreeTableCellFactory<S, UiNavDataItem> {

    private static final Image ICON_INFO =
            new Image(NavDataTableTreeItemCellFactory.class.getResourceAsStream("/img/dialog-information.png"));

    @Getter(lazy = true)
    private final WebViewStage descriptionStage = createDescriptionStage();

    private WebViewStage createDescriptionStage() {
        return new WebViewStage();
    }


    @Override
    public TreeTableCell<S, UiNavDataItem> call(TreeTableColumn<S, UiNavDataItem> param) {

        return new TreeTableCell<>() {

            private final Button button = new Button();

            {
                ImageView imageView = new ImageView(ICON_INFO);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(16.0);
                imageView.setSmooth(true);
                button.setGraphic(imageView);
                button.setBorder(Border.EMPTY);
                button.setBackground(Background.EMPTY);
                button.setPadding(new Insets(0.0));
            }

            @Override
            protected void updateItem(UiNavDataItem value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                setText(value.getName());
                String description = value.getDescription();
                if (description == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(button);
                button.setOnMouseClicked(event -> displayDescription(value.getName(), description));

            }
        };
    }

    private void displayDescription(String name, String description) {
        // display the description (HTML) in a WebView
        WebViewStage stage = getDescriptionStage();
        stage.setTitle(name);
        stage.loadContent(description);
        stage.setAlwaysOnTop(true);
        stage.show();
    }
}
