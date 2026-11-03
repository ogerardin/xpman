package com.ogerardin.xplane.update;

import java.util.List;

public record UpdateStatus(State state, String installedVersion, String latestVersion,
                           List<UpdateOffer> offers, String message) {
    public UpdateStatus {
        offers = state == State.AVAILABLE ? List.copyOf(offers) : List.of();
    }

    /** Keeps the requested channel when offered, otherwise chooses the first available one. */
    public UpdateChannel resolveChannel(UpdateChannel requested) {
        return offers.stream().anyMatch(offer -> offer.channel() == requested) ? requested
                : offers.stream().map(UpdateOffer::channel).findFirst().orElse(requested);
    }

    public enum State { UNSUPPORTED, UNKNOWN, FAILED, DISABLED, LOCKED, CURRENT, AVAILABLE }
}
