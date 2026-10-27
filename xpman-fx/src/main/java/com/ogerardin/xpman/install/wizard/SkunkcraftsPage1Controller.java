package com.ogerardin.xpman.install.wizard;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannel;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
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
    private boolean toggleListenerInstalled = false;

    @FXML
    private Label addonNameLabel;

    @FXML
    private Label currentVersionLabel;

    @FXML
    private Label remoteVersionLabel;

    @FXML
    private Label filesCountLabel;

    @FXML
    private VBox channelBox;

    @FXML
    private RadioButton stableRadio;

    @FXML
    private RadioButton betaRadio;

    @FXML
    private ToggleGroup channelToggle;

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

        // land on a channel that has an update: the requested one if possible, else the sole one
        wizard.setChannel(SkunkcraftsChannel.resolve(wizard.getChannel(), updatable));

        // the channel selector only exists when there is an actual choice to make
        boolean bothChannels = SkunkcraftsChannel.STABLE.hasUpdate(updatable)
                && SkunkcraftsChannel.BETA.hasUpdate(updatable);
        channelBox.setVisible(bothChannels);
        channelBox.setManaged(bothChannels);
        if (bothChannels) {
            stableRadio.setSelected(wizard.getChannel() == SkunkcraftsChannel.STABLE);
            betaRadio.setSelected(wizard.getChannel() == SkunkcraftsChannel.BETA);
            if (!toggleListenerInstalled) {
                channelToggle.selectedToggleProperty().addListener((obs, oldToggle, newToggle) ->
                        switchChannel(newToggle));
                toggleListenerInstalled = true;
            }
        }

        refreshChannelInfo();
    }

    private void switchChannel(Toggle selected) {
        SkunkcraftsChannel newChannel = selected == betaRadio ? SkunkcraftsChannel.BETA : SkunkcraftsChannel.STABLE;
        if (newChannel != wizard.getChannel()) {
            wizard.setChannel(newChannel);
            refreshChannelInfo();
        }
    }

    /** Fetches the selected channel's remote version and update summary in the background. */
    private void refreshChannelInfo() {
        SkunkcraftsUpdatable updatable = wizard.getUpdatable();
        SkunkcraftsChannel channel = wizard.getChannel();

        // Set placeholders for values that require network calls
        remoteVersionLabel.setText("Loading...");
        filesCountLabel.setText("Loading...");
        invalidProperty.set(true);

        // Fetch remote data in background to avoid blocking the UI
        com.ogerardin.xplane.util.AsyncHelper.runAsync(() -> {
            try {
                // Fetch remote version
                String remoteVersion = channel.latestVersion(updatable);

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
                SkunkcraftsUpdateSummary summary = channel.updateSummary(updatable);
                int filesCount = summary.fileCount();

                Platform.runLater(() -> {
                    if (filesCount == 0) {
                        // nothing to download; report the state honestly — a version-label mismatch
                        // without content (e.g. openSAM Library) is not an update
                        filesCountLabel.setText(Objects.equals(updatable.getInstalledVersion(), remoteVersion)
                                ? "Already up to date"
                                : "Files are up to date — local and remote versions differ");
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
