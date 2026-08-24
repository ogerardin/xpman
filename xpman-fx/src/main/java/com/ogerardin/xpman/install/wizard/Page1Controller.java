package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xpman.util.jfx.wizard.ThemedValidationDecoration;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Wizard page 1 controller: allows selection of the source archive.
 * Valid (=Next enabled) only if the file exists.
 */
public class Page1Controller implements Validating {

    @Delegate
    private final ValidationSupport validationSupport = new ValidationSupport();

    @FXML
    private TextField sourcePathField;

    @FXML
    public void initialize() {
        // decorate fields in error
        setErrorDecorationEnabled(true);

        setValidationDecorator(new ThemedValidationDecoration());

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
        return StringUtils.isNotBlank(filename) && Files.exists(Paths.get(filename));
    }

}
