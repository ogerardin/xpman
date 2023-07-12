package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xpman.util.jfx.TreeTableCellFactory;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NavDataTableTreeItemCellFactory<S> implements TreeTableCellFactory<S, UiNavDataItem> {

    private static final Image ICON_INFO =
            new Image(NavDataTableTreeItemCellFactory.class.getResourceAsStream("/img/dialog-information.png"));


    @Override
    public TreeTableCell<S, UiNavDataItem> call(TreeTableColumn<S, UiNavDataItem> param) {

        return new TreeTableCell<S, UiNavDataItem>() {

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
                button.setOnMouseClicked(event -> {
                    // display the description (HTML) in a WebView
                    WebView webView = new WebView();
                    WebEngine engine = webView.getEngine();
                    engine.loadContent(description);
                    Stage stage = new Stage();
                    stage.setTitle("NavData set description");
                    stage.setScene(new Scene(webView));
                    stage.show();
                });

            }
        };
    }
}
