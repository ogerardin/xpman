package com.ogerardin.xplane.skunkcrafts;

import com.ogerardin.xplane.util.progress.ProgressListener;

import java.io.IOException;
import java.util.Arrays;

/**
 * The Skunkcrafts update channels of an addon, each carrying its own availability, version label and update.
 */
public enum SkunkcraftsChannel {

    STABLE("Stable") {
        @Override
        public boolean hasUpdate(SkunkcraftsUpdatable updatable) {
            return updatable.isSkunkcraftsUpdateAvailable();
        }

        @Override
        public String latestVersion(SkunkcraftsUpdatable updatable) {
            return updatable.getSkunkcraftsLatestVersion();
        }

        @Override
        public SkunkcraftsUpdateSummary updateSummary(SkunkcraftsUpdatable updatable) {
            return updatable.getSkunkcraftsUpdateSummary();
        }

        @Override
        public void apply(SkunkcraftsUpdatable updatable, ProgressListener progress)
                throws IOException, SkunkcraftsUpdateException {
            updatable.applySkunkcraftsUpdate(progress);
        }
    },
    BETA("Beta") {
        @Override
        public boolean hasUpdate(SkunkcraftsUpdatable updatable) {
            return updatable.isSkunkcraftsBetaUpdateAvailable();
        }

        @Override
        public String latestVersion(SkunkcraftsUpdatable updatable) {
            return updatable.getSkunkcraftsBetaLatestVersion();
        }

        @Override
        public SkunkcraftsUpdateSummary updateSummary(SkunkcraftsUpdatable updatable) {
            return updatable.getSkunkcraftsBetaUpdateSummary();
        }

        @Override
        public void apply(SkunkcraftsUpdatable updatable, ProgressListener progress)
                throws IOException, SkunkcraftsUpdateException {
            updatable.applySkunkcraftsBetaUpdate(progress);
        }
    };

    private final String label;

    SkunkcraftsChannel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract boolean hasUpdate(SkunkcraftsUpdatable updatable);

    public abstract String latestVersion(SkunkcraftsUpdatable updatable);

    public abstract SkunkcraftsUpdateSummary updateSummary(SkunkcraftsUpdatable updatable);

    public abstract void apply(SkunkcraftsUpdatable updatable, ProgressListener progress)
            throws IOException, SkunkcraftsUpdateException;

    /**
     * The channel to act on: the requested one when it has an update, else the sole available
     * channel, else the requested one — an entry point never lands on a channel with nothing to install.
     */
    public static SkunkcraftsChannel resolve(SkunkcraftsChannel requested, SkunkcraftsUpdatable updatable) {
        if (requested.hasUpdate(updatable)) {
            return requested;
        }
        return Arrays.stream(values())
                .filter(channel -> channel != requested && channel.hasUpdate(updatable))
                .findFirst()
                .orElse(requested);
    }
}
