package com.ogerardin.xpman.util.jfx;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class WrappingCellFactory<C> implements Callback<TableColumn<C, ?>, TableCell<C, ?>> {
    @Override
    public TableCell<C, ?> call(TableColumn<C, ?> param) {
        return new TableCell<C, String>() {
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
