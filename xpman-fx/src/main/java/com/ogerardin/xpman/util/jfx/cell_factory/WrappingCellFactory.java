package com.ogerardin.xpman.util.jfx.cell_factory;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;

/**
 * Factory for a {@code Cell<?, String>} that renders its String content as wrapped text.
 * Borrowed from: https://jluger.de/blog/20160731_javafx_text_rendering_in_tableview.html
 */
public class WrappingCellFactory<S> implements TableCellFactory<S, String> {

    @Override
    public TableCell<S, String> call(TableColumn<S, String> param) {
        return new TableCell<S, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label l = new Label(item);
                    l.setWrapText(true);
                    VBox box = new VBox(l);
                    l.heightProperty().addListener((observable, oldValue, newValue) -> {
                        box.setPrefHeight(newValue.doubleValue() + 7);
                        Platform.runLater(() -> this.getTableRow().requestLayout());
                    });
                    setGraphic(box);
                }
            }
        };
    }
}
