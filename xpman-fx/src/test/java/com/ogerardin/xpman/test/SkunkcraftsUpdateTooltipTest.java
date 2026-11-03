package com.ogerardin.xpman.test;

import com.ogerardin.xpman.util.jfx.UpdateUtil;
import com.ogerardin.xplane.update.*;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pins update tooltip labels to the channels that actually offer content. */
class SkunkcraftsUpdateTooltipTest {

    @Test
    void availableVersionsOnlyListsOfferedChannels() {
        assertEquals("Stable 2.1", UpdateUtil.availableVersions(addon(true, false, "2.1", "2.2-b1")));
        assertEquals("Beta 2.2-b1", UpdateUtil.availableVersions(addon(false, true, "2.1", "2.2-b1")));
        assertEquals("Stable 2.1 · Beta 2.2-b1", UpdateUtil.availableVersions(addon(true, true, "2.1", "2.2-b1")));
        assertEquals("Beta ?", UpdateUtil.availableVersions(addon(false, true, "2.1", null)));
        assertEquals("", UpdateUtil.availableVersions(addon(false, false, "2.1", null)));
    }

    @Test
    void stableOnly() {
        assertEquals("Stable update: 5.4.1", tooltip(true, false, "5.4.1"));
    }

    @Test
    void betaOnly() {
        assertEquals("Beta update: 5.5.0-beta-3", tooltip(false, true, "5.4.1"));
    }

    @Test
    void bothChannels() {
        assertEquals("Stable update: 5.4.1\nBeta update: 5.5.0-beta-3", tooltip(true, true, "5.4.1"));
    }

    @Test
    void missingLabel() {
        assertEquals("Stable update: ?", tooltip(true, false, null));
    }

    @Test
    void noUpdates() {
        assertEquals("", tooltip(false, false, "5.4.1"));
    }

    private String tooltip(boolean stable, boolean beta, String latest) {
        return UpdateUtil.updateTooltip(addon(stable, beta, latest, "5.5.0-beta-3"));
    }

    private Updatable addon(boolean stable, boolean beta, String latest, String betaLatest) {
        return new Updatable() {
            @Override public String getName() { return "Addon"; }
            @Override public String getVersion() { return "5.4.0"; }
            @Override public Optional<UpdateProvider> getUpdateProvider() {
                var offers = Stream.concat(stable ? Stream.of(new UpdateOffer(UpdateChannel.RELEASE, latest, (UpdateAction.Direct) p -> {})) : Stream.empty(),
                        beta ? Stream.of(new UpdateOffer(UpdateChannel.BETA, betaLatest, (UpdateAction.Direct) p -> {})) : Stream.empty()).toList();
                return Optional.of(new UpdateProvider() {
                    public String getName() { return "Test"; }
                    public void checkForUpdates() {}
                    public UpdateStatus getStatus() { return new UpdateStatus(offers.isEmpty() ? UpdateStatus.State.CURRENT : UpdateStatus.State.AVAILABLE,
                            "5.4.0", latest, offers, null); }
                });
            }
        };
    }
}
