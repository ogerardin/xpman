package com.ogerardin.xpman.util.jfx;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.util.jfx.console.ConsoleController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for performing Skunkcrafts updates from the UI.
 */
@UtilityClass
public class SkunkcraftsUpdateUtil {

    /** The same compact action and informational beta marker for cards and table cells. */
    public static HBox updateAction(boolean betaAvailable, String tooltip, Runnable action) {
        Button button = new Button("Update…");
        button.getStyleClass().add("updates-item-button");
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(_ -> action.run());
        HBox graphic = new HBox(6, button);
        graphic.setAlignment(Pos.CENTER_LEFT);
        if (betaAvailable) {
            Label beta = new Label("Beta available");
            beta.getStyleClass().add("update-beta-badge");
            graphic.getChildren().add(beta);
        }
        return graphic;
    }

    /** Lists only channels with pending updates, without changing the addon's latest-version source. */
    public static String updateTooltip(SkunkcraftsUpdatable updatable) {
        return availableChannels(updatable)
                .map(channel -> channel.getLabel() + " update: "
                        + Objects.toString(channel.latestVersion(updatable), "?"))
                .collect(Collectors.joining("\n"));
    }

    /** Concise offered versions for a single dashboard product row. */
    public static String availableVersions(SkunkcraftsUpdatable updatable) {
        return availableChannels(updatable)
                .map(channel -> channel.getLabel() + " " + Objects.toString(channel.latestVersion(updatable), "?"))
                .collect(Collectors.joining(" · "));
    }

    private static Stream<SkunkcraftsChannel> availableChannels(SkunkcraftsUpdatable updatable) {
        return Arrays.stream(SkunkcraftsChannel.values()).filter(channel -> channel.hasUpdate(updatable));
    }

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
