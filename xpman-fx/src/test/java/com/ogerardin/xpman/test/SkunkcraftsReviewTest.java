package com.ogerardin.xpman.test;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.util.AsyncHelper;
import com.ogerardin.xpman.install.wizard.SkunkcraftsUpdateWizard;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.ogerardin.xpman.test.FxTestSupport.onFx;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises the real wizard page, including delayed channel summaries. */
class SkunkcraftsReviewTest {
    @BeforeAll static void startFx() throws Exception { FxTestSupport.start(); }

    @Test
    void soleChannelIsNamedAndBothDefaultToStable() throws Exception {
        for (boolean beta : new boolean[]{false, true}) {
            onFx(() -> {
                var wizard = new SkunkcraftsUpdateWizard(new UpdateTestAddon(!beta, beta));
                var pane = wizard.getDialog().getDialogPane();
                assertEquals(beta ? SkunkcraftsChannel.BETA : SkunkcraftsChannel.STABLE, wizard.getChannel());
                assertEquals(beta ? "Beta" : "Stable", ((Label) pane.lookup("#selectedChannelLabel")).getText());
                assertTrue(pane.lookup("#selectedChannelLabel").isVisible());
                assertFalse(pane.lookup("#channelChoices").isVisible());
                assertEquals("2.0", ((Label) pane.lookup("#currentVersionLabel")).getText());
                return null;
            });
        }
        onFx(() -> {
            var wizard = new SkunkcraftsUpdateWizard(new UpdateTestAddon(true, true));
            assertEquals(SkunkcraftsChannel.STABLE, wizard.getChannel());
            assertTrue(wizard.getDialog().getDialogPane().lookup("#channelChoices").isVisible());
            return null;
        });
    }

    @Test
    void oldCompletionCannotEnableNextOrReplaceNewChannel() throws Exception {
        CountDownLatch stableStarted = new CountDownLatch(1), releaseStable = new CountDownLatch(1);
        CountDownLatch betaStarted = new CountDownLatch(1), releaseBeta = new CountDownLatch(1);
        UpdateTestAddon addon = new UpdateTestAddon(true, true) {
            @Override public SkunkcraftsUpdateSummary getSkunkcraftsUpdateSummary() {
                stableStarted.countDown();
                await(releaseStable);
                return super.getSkunkcraftsUpdateSummary();
            }
            @Override public SkunkcraftsUpdateSummary getSkunkcraftsBetaUpdateSummary() {
                betaStarted.countDown();
                await(releaseBeta);
                return super.getSkunkcraftsBetaUpdateSummary();
            }
        };
        var wizard = onFx(() -> new SkunkcraftsUpdateWizard(addon));
        try {
            assertTrue(stableStarted.await(5, TimeUnit.SECONDS));
            onFx(() -> { ((RadioButton) wizard.getDialog().getDialogPane().lookup("#betaRadio")).fire(); return null; });
            releaseStable.countDown();
            assertTrue(betaStarted.await(5, TimeUnit.SECONDS));
            onFx(() -> {
                assertTrue(wizard.invalidProperty().get(), "Old stable completion must not enable Next");
                assertEquals("Loading...", ((Label) wizard.getDialog().getDialogPane().lookup("#remoteVersionLabel")).getText());
                return null;
            });
        } finally {
            releaseStable.countDown();
            releaseBeta.countDown();
        }
        CountDownLatch drained = new CountDownLatch(1);
        AsyncHelper.runAsync(drained::countDown);
        assertTrue(drained.await(5, TimeUnit.SECONDS));
        onFx(() -> {
            assertEquals("2.2-b1", ((Label) wizard.getDialog().getDialogPane().lookup("#remoteVersionLabel")).getText());
            assertFalse(wizard.invalidProperty().get());
            return null;
        });
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) throw new AssertionError("Test release timed out");
        } catch (InterruptedException e) { throw new AssertionError(e); }
    }
}
