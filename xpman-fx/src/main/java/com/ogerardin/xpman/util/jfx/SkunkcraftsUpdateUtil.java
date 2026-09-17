package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;

/**
 * Utility class for performing Skunkcrafts updates from the UI.
 */
@UtilityClass
public class SkunkcraftsUpdateUtil {

    /**
     * Performs a Skunkcrafts update for the given updatable addon, showing a console dialog for progress.
     *
     * @param updatable the addon to update
     * @param addonName display name for the addon
     * @return inspection result summarizing the update outcome
     */
    @SneakyThrows
    public static InspectionResult performUpdate(
            SkunkcraftsUpdatable updatable,
            String addonName
    ) {
        ConsoleController consoleController = displayConsole("Updating " + addonName + " via Skunkcrafts Updater");
        AsyncHelper.runAsync(() -> updateWithConsole(consoleController, updatable, addonName));
        return InspectionResult.of(InspectionMessage.builder()
                .severity(Severity.INFO)
                .object(addonName)
                .message("Skunkcrafts update started in background")
                .build());
    }

    private static void updateWithConsole(
            ConsoleController consoleController,
            SkunkcraftsUpdatable updatable,
            String addonName
    ) {
        try {
            updatable.applySkunkcraftsUpdate(consoleController);
            consoleController.output("Update completed successfully for " + addonName);
        } catch (SkunkcraftsUpdateException e) {
            consoleController.output("Update failed: " + e.getMessage());
            showErrorDialog("Skunkcrafts Update Failed", e.getMessage());
        } catch (IOException e) {
            consoleController.output("Update failed: " + e.getMessage());
            showErrorDialog("Skunkcrafts Update Failed",
                    "An error occurred while updating " + addonName + ": " + e.getMessage());
        }
    }

    private static ConsoleController displayConsole(@NonNull String title) throws IOException {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader loader = new FXMLLoader(ConsoleController.class.getResource("/fxml/console.fxml"));
        dialog.setDialogPane(loader.load());
        dialog.setTitle(title);
        dialog.show();
        return loader.getController();
    }

    private static void showErrorDialog(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setTitle(title);
            alert.showAndWait();
        });
    }
}
