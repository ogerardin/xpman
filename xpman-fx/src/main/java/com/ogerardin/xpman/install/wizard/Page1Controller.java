package com.ogerardin.xpman.install.wizard;

import com.google.common.base.Strings;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.experimental.Delegate;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.controlsfx.validation.decoration.GraphicValidationDecoration;
import org.controlsfx.validation.decoration.StyleClassValidationDecoration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

public class Page1Controller implements Validating {

    @Delegate
    private final ValidationSupport validationSupport = new ValidationSupport();

    @FXML
    private TextField sourcePathField;

    @FXML
    public void initialize() {
        // decorate fields in error
        setErrorDecorationEnabled(true);

        // don't decorate required fields (because current decoration makes the field look invalid...)
        setValidationDecorator(new GraphicValidationDecoration() {
            @Override
            protected Collection<Decoration> createRequiredDecorations(Control target) {
                return Collections.emptyList();
            }
        });

        registerValidator(sourcePathField, Validator.createPredicateValidator(
                Page1Controller::fileExists, "File does not exist!"));
    }

    @FXML
    public void selectFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        sourcePathField.setText(file.toString());
    }

    private static boolean fileExists(String filename) {
        return !Strings.isNullOrEmpty(filename) && Files.exists(Paths.get(filename));
    }

}
