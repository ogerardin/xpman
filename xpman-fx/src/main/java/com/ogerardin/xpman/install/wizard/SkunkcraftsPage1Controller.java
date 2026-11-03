package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.update.UpdateChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateProvider;
import com.ogerardin.xpman.util.SizeFormat;
import com.ogerardin.xpman.util.jfx.wizard.PageListener;
import com.ogerardin.xpman.util.jfx.wizard.Validating;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.dialog.WizardPane;

import java.util.Objects;

/**
 * Controller for the first page of the Skunkcrafts update wizard.
 * Displays a summary of the update: addon name, current version, selected channel's remote version,
 * and file count. When both channels have updates, lets the user pick the channel to install.
 */
@Slf4j
@RequiredArgsConstructor
public class SkunkcraftsPage1Controller implements Validating, PageListener {

    private final SkunkcraftsUpdateWizard wizard;
    private final BooleanProperty invalidProperty = new SimpleBooleanProperty(true);
    private long refreshGeneration;

    @FXML
    private Label addonNameLabel;

    @FXML
    private Label currentVersionLabel;

    @FXML
    private Label remoteVersionLabel;

    @FXML
    private Label filesCountLabel;

    @FXML
    private Label selectedChannelLabel;

    @FXML
    private HBox channelChoices;

    @FXML
    private RadioButton stableRadio;

    @FXML
    private RadioButton betaRadio;

    @FXML
    private ToggleGroup channelToggle;

    @FXML
    private void initialize() {
        channelToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) switchChannel(newToggle);
        });
    }

    @Override
    public ReadOnlyBooleanProperty invalidProperty() {
        return invalidProperty;
    }

    @Override
    public void onEnteringPage(WizardPane wizardPane) {
        SkunkcraftsUpdateProvider updatable = wizard.getUpdatable();

        // Display addon name (immediate, no network call)
        addonNameLabel.setText(wizard.getAddonName());

        // Display current version (immediate, no network call)
        String currentVersion = updatable.getInstalledVersion();
        currentVersionLabel.setText(currentVersion != null ? currentVersion : "Unknown");

        // land on a channel that has an update: the requested one if possible, else the sole one
        var status = updatable.getStatus();
        wizard.setChannel(status.resolveChannel(wizard.getChannel()));

        // the channel selector only exists when there is an actual choice to make
        boolean bothChannels = status.offers().size() > 1;
        channelChoices.setVisible(bothChannels);
        channelChoices.setManaged(bothChannels);
        selectedChannelLabel.setText(wizard.getChannel().getLabel());
        selectedChannelLabel.setVisible(!bothChannels);
        selectedChannelLabel.setManaged(!bothChannels);
        if (bothChannels) {
            stableRadio.setSelected(wizard.getChannel() == UpdateChannel.RELEASE);
            betaRadio.setSelected(wizard.getChannel() == UpdateChannel.BETA);
        }

        refreshChannelInfo();
    }

    private void switchChannel(Toggle selected) {
        UpdateChannel newChannel = selected == betaRadio ? UpdateChannel.BETA : UpdateChannel.RELEASE;
        if (newChannel != wizard.getChannel()) {
            wizard.setChannel(newChannel);
            refreshChannelInfo();
        }
    }

    /** Fetches the selected channel's remote version and update summary in the background. */
    private void refreshChannelInfo() {
        SkunkcraftsUpdateProvider updatable = wizard.getUpdatable();
        UpdateChannel channel = wizard.getChannel();
        long generation = ++refreshGeneration;

        // Set placeholders for values that require network calls
        remoteVersionLabel.setText("Loading...");
        filesCountLabel.setText("Loading...");
        invalidProperty.set(true);

        // Fetch remote data in background to avoid blocking the UI
        com.ogerardin.xplane.util.AsyncHelper.runAsync(() -> {
            try {
                // Fetch remote version
                String remoteVersion = updatable.getLatestVersion(channel);

                // Fetch update summary
                SkunkcraftsUpdateSummary summary = updatable.getUpdateSummary(channel);
                int filesCount = summary.fileCount();

                Platform.runLater(() -> {
                    if (generation != refreshGeneration) return;
                    remoteVersionLabel.setText(Objects.toString(remoteVersion, "?"));
                    if (filesCount == 0) {
                        // nothing to download; report the state honestly — a version-label mismatch
                        // without content (e.g. openSAM Library) is not an update
                        filesCountLabel.setText(remoteVersion != null && !Objects.equals(updatable.getInstalledVersion(), remoteVersion)
                                ? "Files are up to date — local and remote versions differ"
                                : "Already up to date");
                        invalidProperty.set(true);
                    } else {
                        filesCountLabel.setText(filesCount + " file(s) to update (" + SizeFormat.humanSize(summary.totalSizeBytes()) + ")");
                        invalidProperty.set(false);
                    }
                });

            } catch (Exception e) {
                log.error("Failed to load update information", e);
                Platform.runLater(() -> {
                    if (generation != refreshGeneration) return;
                    invalidProperty.set(true);
                    filesCountLabel.setText("Error: " + e.getMessage());
                });
            }
        });
    }

    @Override
    public void onExitingPage(WizardPane wizardPane) {
        refreshGeneration++;
    }
}
