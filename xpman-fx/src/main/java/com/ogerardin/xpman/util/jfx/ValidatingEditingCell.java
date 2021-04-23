package com.ogerardin.xpman.util.jfx;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.function.Predicate;

/**
 * This clever way of implementing an editable TableCell by setting the editable TextField as the cell's graphic
 * and switching between {@code setContentDisplay(TEXT_ONLY)} and {@code setContentDisplay(GRAPHIC_ONLY)}
 * was inspired by https://stackoverflow.com/questions/35281105/listview-validate-edit-and-prevent-commit/35282166#35282166
 */
public class ValidatingEditingCell<S> extends TableCell<S, String> {

    public ValidatingEditingCell(Predicate<String> validator) {
        setStyle("-fx-font-family: monospace;");
        setGraphic(createTextField(validator));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    private TextField createTextField(Predicate<String> validator) {
        final TextField textField = new TextField();
        final BooleanProperty valid = new SimpleBooleanProperty();

        valid.bind(Bindings.createBooleanBinding(() -> textField.getText() != null && validator.test(textField.getText()),
                textField.textProperty()));
        valid.addListener((obs, wasValid, isValid) -> setValidStyle(textField, isValid));
        setValidStyle(textField, valid.get());

        textField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && valid.get()) {
                commitEdit(textField.getText());
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        return textField;
    }

    private void setValidStyle(TextField textField, boolean isValid) {
        String color = isValid ? "green" : "red";
        textField.setStyle(getStyle() + "-fx-text-fill: " + color + ";");
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
        ((TextField) getGraphic()).setText(empty ? null : item);
        setContentDisplay(isEditing() ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void startEdit() {
        super.startEdit();
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//        textField.selectAll();
//        getGraphic().requestFocus();
    }
}