package com.ogerardin.xplane.test.update;

import com.ogerardin.xplane.update.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Channel selection is independent of the provider and its release mechanism. */
class UpdateChannelTest {
    @Test
    void requestedChannelWinsWhenBothAreOffered() {
        var status = status(UpdateChannel.RELEASE, UpdateChannel.BETA);
        assertEquals(UpdateChannel.RELEASE, status.resolveChannel(UpdateChannel.RELEASE));
        assertEquals(UpdateChannel.BETA, status.resolveChannel(UpdateChannel.BETA));
    }

    @Test
    void selectionFallsBackToTheOnlyOfferedChannel() {
        assertEquals(UpdateChannel.BETA, status(UpdateChannel.BETA).resolveChannel(UpdateChannel.RELEASE));
        assertEquals(UpdateChannel.RELEASE, status(UpdateChannel.RELEASE).resolveChannel(UpdateChannel.BETA));
    }

    @Test
    void noOfferKeepsRequestedChannelWithoutInventingAnUpdate() {
        var status = status();
        assertEquals(UpdateChannel.BETA, status.resolveChannel(UpdateChannel.BETA));
        assertTrue(status.offers().isEmpty());
    }

    private UpdateStatus status(UpdateChannel... channels) {
        var offers = java.util.Arrays.stream(channels)
                .map(channel -> new UpdateOffer(channel, "2.0", (UpdateAction.Direct) p -> {})).toList();
        return new UpdateStatus(offers.isEmpty() ? UpdateStatus.State.CURRENT : UpdateStatus.State.AVAILABLE,
                "1.0", "2.0", offers, null);
    }
}
