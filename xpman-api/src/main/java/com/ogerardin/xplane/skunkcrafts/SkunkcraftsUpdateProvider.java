package com.ogerardin.xplane.skunkcrafts;

import com.ogerardin.xplane.update.*;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Owns an add-on's Skunkcrafts protocol state; domain objects expose only the generic capability. */
@RequiredArgsConstructor
public class SkunkcraftsUpdateProvider implements UpdateProvider, SkunkcraftsUpdatable {
    private final Supplier<Path> folder;
    @Getter private final SkunkcraftsConfig skunkcraftsConfig;
    @Getter private final SkunkcraftsConfig skunkcraftsBetaConfig;
    private final Supplier<String> version;
    private final BooleanSupplier enabled;
    private final AtomicReference<String> skunkcraftsLatestVersion = new AtomicReference<>();
    private final AtomicReference<SkunkcraftsChannelStatus> skunkcraftsChannelStatus = 
            new AtomicReference<>(SkunkcraftsChannelStatus.NONE);
    private final AtomicReference<UpdateStatus.State> state = new AtomicReference<>(UpdateStatus.State.UNKNOWN);
    private final AtomicReference<String> failure = new AtomicReference<>();

    public SkunkcraftsUpdateProvider(Path folder, SkunkcraftsConfig config, SkunkcraftsConfig beta,
                                    Supplier<String> version, BooleanSupplier enabled) {
        this(() -> folder, config, beta, version, enabled);
    }

    /** Creates a provider from the Skunkcrafts config discovered in the add-on's folder, or empty if none. */
    public static Optional<UpdateProvider> discover(
            Supplier<Path> folder, Supplier<String> version, BooleanSupplier enabled) {
        SkunkcraftsConfig config = SkunkcraftsUpdater.findConfig(folder.get());
        if (config == null) {
            return Optional.empty();
        }
        return Optional.of(new SkunkcraftsUpdateProvider(
                folder, config, SkunkcraftsUpdater.findBetaConfig(folder.get()), version, enabled));
    }

    @Override public Path getSkunkcraftsFolder() { return folder.get(); }

    @Override public String getName() { return "Skunkcrafts"; }
    @Override public String getVersion() { return version.get(); }

    @Override public String getSkunkcraftsLatestVersion() { return skunkcraftsLatestVersion.get(); }
    @Override public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() { return skunkcraftsChannelStatus.get(); }

    @Override
    public boolean isSkunkcraftsUpdatable() {
        return enabled.getAsBoolean() && SkunkcraftsUpdatable.super.isSkunkcraftsUpdatable();
    }

    @Override
    public synchronized void checkForUpdates() {
        if (!isSkunkcraftsUpdatable()) return;
        try {
            failure.set(null);
            skunkcraftsChannelStatus.set(loadChannelStatus());
            skunkcraftsLatestVersion.set(fetchVersion(skunkcraftsConfig.moduleUrl()));
            if (skunkcraftsChannelStatus.get().betaUpdateAvailable()) {
                skunkcraftsChannelStatus.set(new SkunkcraftsChannelStatus(skunkcraftsChannelStatus.get().stableUpdateAvailable(),
                        true, fetchVersion(skunkcraftsBetaConfig.moduleUrl())));
            }
            state.set(UpdateStatus.State.CURRENT);
        } catch (Exception e) {
            skunkcraftsChannelStatus.set(SkunkcraftsChannelStatus.NONE);
            failure.set(e.getMessage());
            state.set(UpdateStatus.State.FAILED);
        }
    }

    /** Loads channel status, falling back to stable-only when beta discovery fails. */
    private SkunkcraftsChannelStatus loadChannelStatus() throws IOException {
        try {
            return SkunkcraftsUpdater.computeChannelStatusChecked(
                    getSkunkcraftsFolder(), skunkcraftsConfig, skunkcraftsBetaConfig);
        } catch (IOException e) {
            if (skunkcraftsBetaConfig == null) throw e;
            failure.set("Beta update check failed: " + e.getMessage());
            return SkunkcraftsUpdater.computeChannelStatusChecked(
                    getSkunkcraftsFolder(), skunkcraftsConfig, null);
        }
    }

    private String fetchVersion(String moduleUrl) {
        try { return SkunkcraftsUpdater.fetchRemoteVersionChecked(moduleUrl); }
        catch (IOException e) {
            String message = "Version check failed: " + e.getMessage();
            String currentFailure = failure.get();
            failure.set(currentFailure == null ? message : currentFailure + "; " + message);
            return null;
        }
    }

    @Override
    public synchronized UpdateStatus getStatus() {
        var current = currentState();
        List<UpdateOffer> offers = current == UpdateStatus.State.CURRENT
                ? Arrays.stream(UpdateChannel.values()).filter(this::hasUpdate)
                .map(c -> new UpdateOffer(c, getLatestVersion(c),
                        (UpdateAction.Direct) progress -> apply(c, progress))).toList() : List.of();
        var reported = reportedState(current, offers);
        return new UpdateStatus(reported,
                getInstalledVersion(), skunkcraftsLatestVersion.get(), offers, failure.get());
    }

    /** The live state ignoring pending offers: disabled, locked, or the last check's outcome. */
    private UpdateStatus.State currentState() {
        if (!enabled.getAsBoolean()) {
            return UpdateStatus.State.DISABLED;
        }
        if (isSkunkcraftsLocked()) {
            return UpdateStatus.State.LOCKED;
        }
        if (!isSkunkcraftsUpdatable()) {
            return UpdateStatus.State.DISABLED;
        }
        return state.get();
    }

    /** The state to report: offers win, then a metadata failure, otherwise the live state. */
    private UpdateStatus.State reportedState(UpdateStatus.State current, List<UpdateOffer> offers) {
        if (!offers.isEmpty()) {
            return UpdateStatus.State.AVAILABLE;
        }
        if (current == UpdateStatus.State.CURRENT && failure.get() != null) {
            return UpdateStatus.State.FAILED;
        }
        return current;
    }

    private boolean hasUpdate(UpdateChannel channel) {
        return switch (channel) {
            case RELEASE -> isSkunkcraftsUpdateAvailable();
            case BETA -> isSkunkcraftsBetaUpdateAvailable();
        };
    }

    public String getLatestVersion(UpdateChannel channel) {
        return switch (channel) {
            case RELEASE -> getSkunkcraftsLatestVersion();
            case BETA -> getSkunkcraftsBetaLatestVersion();
        };
    }

    public SkunkcraftsUpdateSummary getUpdateSummary(UpdateChannel channel) throws IOException {
        return switch (channel) {
            case RELEASE -> getSkunkcraftsUpdateSummary();
            case BETA -> getSkunkcraftsBetaUpdateSummary();
        };
    }

    private void apply(UpdateChannel channel, ProgressListener progress) throws IOException {
        if (!isSkunkcraftsUpdatable() || (channel == UpdateChannel.BETA
                && SkunkcraftsUpdater.ignoreBetaChannel.get().getAsBoolean())) {
            throw new IOException("Update is no longer available");
        }
        try {
            switch (channel) {
                case RELEASE -> applySkunkcraftsUpdate(progress);
                case BETA -> applySkunkcraftsBetaUpdate(progress);
            }
        }
        catch (SkunkcraftsUpdateException e) { throw new IOException(e.getMessage(), e); }
    }
}
