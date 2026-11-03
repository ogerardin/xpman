package com.ogerardin.xplane.skunkcrafts;

import com.ogerardin.xplane.update.*;
import com.ogerardin.xplane.util.progress.ProgressListener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
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

    @Override public Path getSkunkcraftsFolder() { return folder.get(); }

    @Override public String getName() { return "Skunkcrafts"; }
    @Override public String getVersion() { return version.get(); }

    public String getSkunkcraftsLatestVersion() { return skunkcraftsLatestVersion.get(); }
    public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() { return skunkcraftsChannelStatus.get(); }

    @Override
    public boolean isSkunkcraftsUpdatable() {
        return enabled.getAsBoolean() && SkunkcraftsUpdatable.super.isSkunkcraftsUpdatable();
    }

    @Override
    public synchronized void checkForUpdates() {
        if (!isSkunkcraftsUpdatable()) return;
        try {
            failure.set(null);
            try {
                skunkcraftsChannelStatus.set(SkunkcraftsUpdater.computeChannelStatusChecked(
                        getSkunkcraftsFolder(), skunkcraftsConfig, skunkcraftsBetaConfig));
            } catch (IOException e) {
                if (skunkcraftsBetaConfig == null) throw e;
                skunkcraftsChannelStatus.set(SkunkcraftsUpdater.computeChannelStatusChecked(
                        getSkunkcraftsFolder(), skunkcraftsConfig, null));
                failure.set("Beta update check failed: " + e.getMessage());
            }
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
        var current = !enabled.getAsBoolean() ? UpdateStatus.State.DISABLED
                : isSkunkcraftsLocked() ? UpdateStatus.State.LOCKED
                : !isSkunkcraftsUpdatable() ? UpdateStatus.State.DISABLED : state.get();
        List<UpdateOffer> offers = current == UpdateStatus.State.CURRENT
                ? Arrays.stream(UpdateChannel.values()).filter(this::hasUpdate)
                .map(c -> new UpdateOffer(c, getLatestVersion(c),
                        (UpdateAction.Direct) progress -> apply(c, progress))).toList() : List.of();
        var reported = !offers.isEmpty() ? UpdateStatus.State.AVAILABLE
                : current == UpdateStatus.State.CURRENT && failure.get() != null ? UpdateStatus.State.FAILED : current;
        return new UpdateStatus(reported,
                getInstalledVersion(), skunkcraftsLatestVersion.get(), offers, failure.get());
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
