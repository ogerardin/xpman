package com.ogerardin.xpman.util.jfx.cell_factory;

import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Factory for a {@code TableCell<S, Boolean>} that renders {@code true} as a centered pill
 * labelled {@code text} and styled with {@code styleClass}, and {@code false} as nothing.
 * Both come from FXML, e.g. {@code <PillCellFactory text="Airport" styleClass="pill-success"/>}.
 */
public class PillCellFactory<S> implements TableCellFactory<S, Boolean> {

    private final String text;
    private final String styleClass;

    public PillCellFactory(@NamedArg("text") String text, @NamedArg("styleClass") String styleClass) {
        this.text = text;
        this.styleClass = styleClass;
    }

    @Override
    public TableCell<S, Boolean> call(TableColumn<S, Boolean> param) {
        Label pill = new Label(text);
        pill.getStyleClass().add(styleClass);
        TableCell<S, Boolean> cell = new TableCell<>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value, empty);
                setText(null);
                setGraphic(Boolean.TRUE.equals(value) ? pill : null);
            }
        };
        cell.setAlignment(Pos.CENTER);
        return cell;
    }

}