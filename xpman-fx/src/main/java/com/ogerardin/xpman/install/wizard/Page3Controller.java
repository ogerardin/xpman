package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.config.install.GenericInstaller;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ogerardin.xpman.util.jfx.wizard.Wizard.disableButton;

@Slf4j
public class Page3Controller implements PageListener {

    @FXML
    private ProgressBar progress;

    @FXML
    private Label fileLabel;

    @NonNull
    private final GenericInstaller installer;

    public Page3Controller(InstallWizard wizard) {
        installer = wizard.getInstaller();
    }

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        // disable 'Previous' and 'Finish' buttons
        disableButton(wizardPane, ButtonBar.ButtonData.BACK_PREVIOUS, true);
        disableButton(wizardPane, ButtonBar.ButtonData.NEXT_FORWARD, true);

        // run the installer in new thread while monitoring progress
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
