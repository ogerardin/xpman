package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xpman.panels.home.HomeController;
import com.ogerardin.xpman.panels.home.XPlaneUpdateDialog;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ogerardin.xpman.test.FxTestSupport.onFx;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises channel review and cancellation without launching an external installer. */
class XPlaneUpdateDialogTest {
    private static final XPlaneReleaseInfo STABLE = new XPlaneReleaseInfo("12.4.1", Optional.of("https://example.com/stable"));
    private static final XPlaneReleaseInfo BETA = new XPlaneReleaseInfo("12.5.0-b1", Optional.of("https://example.com/beta"));

    @BeforeAll static void startFx() throws Exception { FxTestSupport.start(); }

    @Test
    void stableDefaultAndBetaSelectionControlScreenshotGuidanceAndNotes() throws Exception {
        onFx(() -> {
            var dialog = new XPlaneUpdateDialog("12.4.0", STABLE, BETA);
            var pane = dialog.getDialogPane();
            var screenshot = (ImageView) pane.lookup("#betaScreenshot");
            var notes = (Hyperlink) pane.lookup("#releaseNotes");
            var guidance = (Label) pane.lookup("#installerGuidance");
            assertTrue(((RadioButton) pane.lookup("#stableRadio")).isSelected());
            assertFalse(screenshot.isVisible());
            assertFalse(screenshot.isManaged());
            assertTrue(guidance.getText().contains("unchecked"));
            assertEquals("https://example.com/stable", notes.getUserData());
            assertEquals("When the installer opens, click ‘Update X-Plane’ to continue.",
                    ((Label) pane.lookup("#installerInstructions")).getText());
            ((RadioButton) pane.lookup("#betaRadio")).fire();
            assertTrue(screenshot.isVisible());
            assertTrue(screenshot.isManaged());
            assertNotNull(screenshot.getImage());
            assertFalse(screenshot.getImage().isError());
            assertTrue(guidance.getText().contains("check the box"));
            assertEquals("https://example.com/beta", notes.getUserData());
            assertEquals(BETA, dialog.getResultConverter().call(confirm(dialog)));
            ((RadioButton) pane.lookup("#stableRadio")).fire();
            assertFalse(screenshot.isVisible());
            assertFalse(screenshot.isManaged());
            assertEquals(STABLE, dialog.getResultConverter().call(confirm(dialog)));
            assertNull(dialog.getResultConverter().call(ButtonType.CANCEL));
            return null;
        });
    }

    @Test
    void soleOfferIsExplicitAndMissingNotesDoNotCreateADeadLink() throws Exception {
        onFx(() -> {
            var betaOnly = new XPlaneUpdateDialog(null, null, new XPlaneReleaseInfo(null, Optional.empty()));
            var pane = betaOnly.getDialogPane();
            assertEquals("Beta ?", ((Label) pane.lookup("#selectedChannelLabel")).getText());
            assertFalse(pane.lookup("#channelChoices").isVisible());
            assertTrue(pane.lookup("#betaScreenshot").isVisible());
            assertFalse(pane.lookup("#releaseNotes").isVisible());
            assertFalse(pane.lookup("#releaseNotes").isManaged());
            var stableOnly = new XPlaneUpdateDialog("12.4.0", STABLE, null);
            assertEquals("Stable 12.4.1", ((Label) stableOnly.getDialogPane().lookup("#selectedChannelLabel")).getText());
            assertFalse(stableOnly.getDialogPane().lookup("#betaScreenshot").isVisible());
            assertThrows(IllegalArgumentException.class, () -> new XPlaneUpdateDialog("12.4.0", null, null));
            return null;
        });
    }

    @Test
    void selectingBetaMakesRoomForTheScreenshotInTheShownDialog() throws Exception {
        var dialog = onFx(() -> {
            var review = new XPlaneUpdateDialog("12.4.0", STABLE, BETA);
            review.show();
            return review;
        });
        try {
            double stableHeight = onFx(() -> dialog.getDialogPane().getScene().getWindow().getHeight());
            onFx(() -> { ((RadioButton) dialog.getDialogPane().lookup("#betaRadio")).fire(); return null; });
            onFx(() -> {
                assertTrue(dialog.getDialogPane().getScene().getWindow().getHeight() > stableHeight,
                        "The screenshot must fit, not be clipped by the original stable-only dialog bounds");
                return null;
            });
        } finally {
            onFx(() -> { dialog.close(); return null; });
        }
    }

    @Test
    void cancelAndWindowCloseNeverLaunchAndConfirmationLaunchesOnce() throws Exception {
        onFx(() -> {
            AtomicInteger launches = new AtomicInteger();
            var method = HomeController.class.getDeclaredMethod("reviewXPlaneUpdate", XPlaneUpdateDialog.class, Runnable.class);
            method.setAccessible(true);
            for (int action = 0; action < 3; action++) {
                var dialog = new XPlaneUpdateDialog("12.4.0", STABLE, BETA);
                int choice = action;
                Platform.runLater(() -> {
                    if (choice == 0) ((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).fire();
                    else if (choice == 1) dialog.close();
                    else ((Button) dialog.getDialogPane().lookupButton(confirm(dialog))).fire();
                });
                method.invoke(null, dialog, (Runnable) launches::incrementAndGet);
                assertEquals(action == 2 ? 1 : 0, launches.get());
            }
            return null;
        });
    }

    private ButtonType confirm(XPlaneUpdateDialog dialog) {
        var button = dialog.getDialogPane().getButtonTypes().stream()
                .filter(type -> type.getButtonData() == ButtonData.OK_DONE).findFirst().orElseThrow();
        assertEquals("Open X-Plane Installer", button.getText());
        return button;
    }
}
