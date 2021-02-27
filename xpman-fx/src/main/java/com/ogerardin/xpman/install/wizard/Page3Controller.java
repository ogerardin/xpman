package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.install.GenericInstaller;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import static com.ogerardin.xpman.util.jfx.wizard.Wizard.disableButton;

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
        Thread thread = new Thread(() -> installer.install(Page3Controller.this::updateProgress));
        thread.start();
    }

    private void updateProgress(double p, String message) {
        Platform.runLater(() -> {
//            log.debug("Progress: {}", p);
            progress.setProgress(p);
            fileLabel.setText(p == 1.0 ? "Done!" : message);
        });
    }
}
