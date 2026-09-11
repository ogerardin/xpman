package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xplane.install.InstallationException;
import com.ogerardin.xpman.util.jfx.ErrorDialog;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import static com.ogerardin.xpman.util.jfx.wizard.Wizard.disableButton;

/**
 * Wizard page 3 controller: performs actual installation with progress display.
 */
@Slf4j
@RequiredArgsConstructor
public class Page3Controller implements PageListener {

    @NonNull
    private final InstallWizard wizard;

    @FXML
    private ProgressBar progress;

    @FXML
    private Label fileLabel;

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // disable 'Previous' and 'Finish' buttons
        disableButton(wizardPane, ButtonData.BACK_PREVIOUS, true);
        disableButton(wizardPane, ButtonData.NEXT_FORWARD, true);

        // run the installer in new thread while monitoring progress
        GenericInstaller installer = wizard.getInstaller();
        Thread thread = new Thread(() -> {
            try {
                installer.install(this::updateProgress);
            } catch (InstallationException e) {
                log.error("Installation failed", e);
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
