package com.ogerardin.xpman.panels.home;

import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.util.platform.Platforms;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Reviews offered X-Plane channels and explains the manual steps in the external installer. */
public class XPlaneUpdateDialog extends Dialog<XPlaneReleaseInfo> {

    public XPlaneUpdateDialog(String currentVersion, XPlaneReleaseInfo stableOffer, XPlaneReleaseInfo betaOffer) {
        if (stableOffer == null && betaOffer == null) throw new IllegalArgumentException("No X-Plane update offered");
        setTitle("Update X-Plane");
        setHeaderText("Review update information");
        ButtonType openInstaller = new ButtonType("Open X-Plane Installer", ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, openInstaller);

        BooleanProperty betaSelected = new SimpleBooleanProperty(stableOffer == null);
        boolean both = stableOffer != null && betaOffer != null;
        ToggleGroup channels = new ToggleGroup();
        RadioButton stable = new RadioButton(versionText("Stable", stableOffer));
        stable.setId("stableRadio");
        stable.setToggleGroup(channels);
        RadioButton beta = new RadioButton(versionText("Beta", betaOffer));
        beta.setId("betaRadio");
        beta.setToggleGroup(channels);
        channels.selectToggle(stableOffer != null ? stable : beta);
        channels.selectedToggleProperty().addListener((_, _, selected) -> betaSelected.set(selected == beta));
        HBox choices = new HBox(16, stable, beta);
        choices.setId("channelChoices");
        choices.setVisible(both);
        choices.setManaged(both);

        Label soleChannel = new Label(stableOffer != null ? stable.getText() : beta.getText());
        soleChannel.setId("selectedChannelLabel");
        soleChannel.setVisible(!both);
        soleChannel.setManaged(!both);
        Label instructions = new Label("When the installer opens, click ‘Update X-Plane’ to continue.");
        instructions.setId("installerInstructions");
        instructions.setWrapText(true);
        Label guidance = new Label();
        guidance.setId("installerGuidance");
        guidance.setWrapText(true);
        guidance.textProperty().bind(Bindings.when(betaSelected)
                .then("On the installer's Welcome page, check the box ‘Check for new betas as well as updates’.")
                .otherwise("On the installer's Welcome page, leave ‘Check for new betas as well as updates’ unchecked."));

        ImageView screenshot = new ImageView(new Image(Objects.requireNonNull(
                XPlaneUpdateDialog.class.getResource("/img/screenshot_beta.png")).toExternalForm()));
        screenshot.setId("betaScreenshot");
        screenshot.setFitWidth(540);
        screenshot.setFitHeight(300);
        screenshot.setPreserveRatio(true);
        screenshot.visibleProperty().bind(betaSelected);
        screenshot.managedProperty().bind(screenshot.visibleProperty());

        Hyperlink notes = new Hyperlink("Release notes");
        notes.setId("releaseNotes");
        Runnable updateNotes = () -> {
            XPlaneReleaseInfo selected = betaSelected.get() ? betaOffer : stableOffer;
            String url = selected.releaseNotesUrl().orElse(null);
            notes.setVisible(url != null);
            notes.setManaged(url != null);
            notes.setUserData(url);
            notes.setOnAction(url == null ? null : _ -> Platforms.getCurrent().openUrl(url));
        };
        betaSelected.addListener((_, _, _) -> {
            updateNotes.run();
            if (isShowing()) getDialogPane().getScene().getWindow().sizeToScene();
        });
        updateNotes.run();

        VBox content = new VBox(12,
                new Label("Installed: " + Objects.toString(currentVersion, "?")),
                new Label("Channel:"), choices, soleChannel, notes, instructions, guidance, screenshot);
        content.setPrefWidth(560);
        getDialogPane().setContent(content);
        setResultConverter(button -> button == openInstaller ? (betaSelected.get() ? betaOffer : stableOffer) : null);
    }

    /** Offered labels for the single X-Plane product row; absence is distinct from an unknown label. */
    static String availableVersions(XPlaneReleaseInfo stableOffer, XPlaneReleaseInfo betaOffer) {
        return Stream.of(stableOffer == null ? null : versionText("Stable", stableOffer),
                        betaOffer == null ? null : versionText("Beta", betaOffer))
                .filter(Objects::nonNull).collect(Collectors.joining(" · "));
    }

    private static String versionText(String channel, XPlaneReleaseInfo release) {
        return channel + " " + (release == null ? "?" : Objects.toString(release.version(), "?"));
    }
}
