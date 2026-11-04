package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.update.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/** Inspections consume provider offers, never infer updates from version labels. */
class UpdateAvailableInspectionTest {
    @Test
    void channelOffersProduceSeparateWarnings() {
        var status = new UpdateStatus(UpdateStatus.State.AVAILABLE, "1.0", "2.0", List.of(
                new UpdateOffer(UpdateChannel.RELEASE, "2.0", (UpdateAction.Direct) p -> {}),
                new UpdateOffer(UpdateChannel.BETA, "3.0-beta", (UpdateAction.Direct) p -> {})), null);
        var result = UpdateAvailableInspection.INSTANCE.inspect(item(status));
        assertEquals(2, result.size());
        assertEquals("MyAddon", result.get(0).getObject());
        assertEquals("WARN", result.get(0).getSeverity().name());
        assertEquals("Update available: 2.0", result.get(0).getMessage());
        assertEquals("Beta update available: 3.0-beta", result.get(1).getMessage());
    }

    @Test
    void manualDownloadOfferProducesWarning() {
        var status = new UpdateStatus(UpdateStatus.State.AVAILABLE, "1.0", "2.0", List.of(
                new UpdateOffer(UpdateChannel.RELEASE, "2.0", (UpdateAction.DownloadPage) () -> null)), null);
        assertEquals(1, UpdateAvailableInspection.INSTANCE.inspect(item(status)).size());
    }

    @Test
    void labelsAloneAndUnavailableStatesDoNotProduceUpdateWarnings() {
        for (var state : List.of(UpdateStatus.State.CURRENT, UpdateStatus.State.UNKNOWN,
                UpdateStatus.State.DISABLED, UpdateStatus.State.LOCKED, UpdateStatus.State.UNSUPPORTED)) {
            var status = new UpdateStatus(state, "1.0", "2.0", List.of(), null);
            assertTrue(UpdateAvailableInspection.INSTANCE.inspect(item(status)).isEmpty());
        }
    }

    @Test
    void failedDiscoveryIsReportedRatherThanSilentlyCurrent() {
        var result = UpdateAvailableInspection.INSTANCE.inspect(item(
                new UpdateStatus(UpdateStatus.State.FAILED, "1.0", null, List.of(), "offline")));
        assertEquals(1, result.size());
        assertTrue(result.get(0).getMessage().contains("offline"));
    }

    private Updatable item(UpdateStatus status) {
        return new Updatable() {
            @Override public String getName() { return "MyAddon"; }
            @Override public String getVersion() { return "1.0"; }
            @Override public Optional<UpdateProvider> getUpdateProvider() {
                return Optional.of(new UpdateProvider() {
                    @Override public String getName() { return "Test"; }
                    /** No-op: this fixture serves a precomputed status. */
                    @Override public void checkForUpdates() { /* fixture serves a precomputed status */ }
                    @Override public UpdateStatus getStatus() { return status; }
                });
            }
        };
    }
}
