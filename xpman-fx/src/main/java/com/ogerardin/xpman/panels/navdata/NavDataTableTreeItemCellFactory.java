package com.ogerardin.xpman.panels.navdata;

import com.ogerardin.xpman.util.jfx.TreeTableCellFactory;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;

public class NavDataTableTreeItemCellFactory<S> implements TreeTableCellFactory<S, UiNavDataItem> {

    @Override
    public TreeTableCell<S, UiNavDataItem> call(TreeTableColumn<S, UiNavDataItem> param) {
        return new TreeTableCell<S, UiNavDataItem>() {
            @Override
            protected void updateItem(UiNavDataItem value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(value.getName());
                    String description = value.getDescription();
                    if (description != null) {
                        WebView webView = new WebView();
                        WebEngine engine = webView.getEngine();
                        engine.loadContent(description);
                        Tooltip tooltip = new Tooltip();
                        tooltip.setShowDelay(new Duration(500.0));
                        tooltip.setShowDuration(Duration.INDEFINITE);
                        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        tooltip.setGraphic(webView);
                        setTooltip(tooltip);
                    }
                    else {
                        setTooltip(null);
                    }
                }
            }
        };
    }
}
