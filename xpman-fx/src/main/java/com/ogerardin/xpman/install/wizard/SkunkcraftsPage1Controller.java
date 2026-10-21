package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xpman.util.SizeFormat;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

/**
 * Controller for the first page of the Skunkcrafts update wizard.
 * Displays a summary of the update: addon name, current version, remote version, and file count.
 */
@Slf4j
@RequiredArgsConstructor
public class SkunkcraftsPage1Controller implements Validating, PageListener {

    private final SkunkcraftsUpdateWizard wizard;
    private final BooleanProperty invalidProperty = new SimpleBooleanProperty(true);

    @FXML
    private Label addonNameLabel;

    @FXML
    private Label currentVersionLabel;

    @FXML
    private Label remoteVersionLabel;

    @FXML
    private Label filesCountLabel;

    @Override
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        SkunkcraftsUpdatable updatable = wizard.getUpdatable();

        // Display addon name (immediate, no network call)
        addonNameLabel.setText(updatable.getName());

        // Display current version (immediate, no network call)
        String currentVersion = updatable.getVersion();
        currentVersionLabel.setText(currentVersion != null ? currentVersion : "Unknown");

        // Set placeholders for values that require network calls
        remoteVersionLabel.setText("Loading...");
        filesCountLabel.setText("Loading...");
        invalidProperty.set(true);

        // Fetch remote data in background to avoid blocking the UI
        com.ogerardin.xplane.util.AsyncHelper.runAsync(() -> {
            try {
                // Fetch remote version
                String remoteVersion = updatable.getSkunkcraftsLatestVersion();
                
                Platform.runLater(() -> {
                    if (remoteVersion == null) {
                        invalidProperty.set(true);
                        remoteVersionLabel.setText("Could not fetch remote version");
                        filesCountLabel.setText("");
                        return;
                    }
                    remoteVersionLabel.setText(remoteVersion);
                });

                // Fetch update summary
                SkunkcraftsUpdateSummary summary = updatable.getSkunkcraftsUpdateSummary();
                int filesCount = summary.fileCount();
                
                Platform.runLater(() -> {
                    if (filesCount == 0) {
                        filesCountLabel.setText("Already up to date");
                        invalidProperty.set(true);
                    } else {
                        filesCountLabel.setText(filesCount + " file(s) to update (" + SizeFormat.humanSize(summary.totalSizeBytes()) + ")");
                        invalidProperty.set(false);
                    }
                });

            } catch (Exception e) {
                log.error("Failed to load update information", e);
                Platform.runLater(() -> {
                    invalidProperty.set(true);
                    filesCountLabel.setText("Error: " + e.getMessage());
                });
            }
        });
    }
}
