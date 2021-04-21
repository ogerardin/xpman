package com.ogerardin.xpman.util.jfx;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Font;
import lombok.Synchronized;

/**
 * Factory for a {@code TableCell<?, ?>}
 */
public class FontCellFactory<S> implements TableCellFactory<S, String> {
    
    @Override
    public TableCell<S, String> call(TableColumn<S, String> param) {
        return new TableCell<S, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                setFont(Font.font("Monospaced"));
                setText(empty ? "" : value);
            }
        };
    }

//    public static void main(String[] args) {
//        Font.getFamilies().forEach(System.out::println);
//    }

}
