package com.ogerardin.xpman.diag;

import com.ogerardin.xplane.inspection.Fix;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * Shared confirmation and error handling for {@link Fix} execution.
 *
 * <p>Both the inspection dialog and the navdata card delegate to this class so that confirm
 * and error handling live in one place. The fix's {@link Fix#apply()} method is expected to
 * run synchronously; for fixes that do async work (e.g. reload), the fix itself handles the
 * threading.</p>
 */
@UtilityClass
public class FixRunner {

    /**
     * Asks the user to confirm, runs the fix, and shows an error dialog on failure.
     *
     * @return true only when the fix was applied
     */
    public static boolean run(Fix fix, Node owner) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText(fix.label() + "?");
        confirm.initOwner(owner.getScene().getWindow());

        Optional<ButtonType> clicked = confirm.showAndWait();
        if (clicked.isEmpty() || clicked.get() != ButtonType.OK) {
            return false;
        }

        try {
            fix.apply();
            return true;
        } catch (RuntimeException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Fix failed");
            error.setHeaderText(fix.label());
            error.setContentText(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            error.initOwner(owner.getScene().getWindow());
            error.showAndWait();
            return false;
        }
    }
}
