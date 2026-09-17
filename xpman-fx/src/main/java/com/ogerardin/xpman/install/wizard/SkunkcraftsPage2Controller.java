package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xpman.util.jfx.ErrorDialog;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import static com.ogerardin.xpman.util.jfx.wizard.Wizard.disableButton;
import static javafx.scene.control.ButtonBar.ButtonData;

/**
 * Controller for the second page of the Skunkcrafts update wizard.
 * Displays progress during the update process.
 */
@Slf4j
@RequiredArgsConstructor
public class SkunkcraftsPage2Controller implements PageListener {

    private final SkunkcraftsUpdateWizard wizard;

    @FXML
    private ProgressBar progress;

    @FXML
    private Label fileLabel;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // Disable navigation buttons
        disableButton(wizardPane, ButtonData.BACK_PREVIOUS, true);
        disableButton(wizardPane, ButtonData.NEXT_FORWARD, true);

        SkunkcraftsUpdatable updatable = wizard.getUpdatable();

        // Run update in background thread
        Thread thread = new Thread(() -> {
            try {
                updatable.applySkunkcraftsUpdate(this::updateProgress);
                Platform.runLater(() -> {
                    progress.setProgress(1.0);
                    fileLabel.setText("Update complete!");
                });
            } catch (Exception e) {
                log.error("Update failed", e);
                Platform.runLater(() -> ErrorDialog.showError(e, null));
            }
        });
        thread.start();
    }

    private void updateProgress(Double p, String message) {
        Platform.runLater(() -> {
            if (p != null) {
                progress.setProgress(p);
            }
            if (message != null) {
                fileLabel.setText(p != null && p == 1.0 ? "Done!" : message);
            }
        });
    }
}
