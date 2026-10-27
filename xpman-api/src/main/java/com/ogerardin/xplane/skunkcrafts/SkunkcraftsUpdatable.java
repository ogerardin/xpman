package com.ogerardin.xplane.skunkcrafts;

import java.io.IOException;
import java.nio.file.Path;

import com.ogerardin.xplane.util.progress.ProgressListener;

/**
 * Implemented by domain objects (Plugin, Aircraft, SceneryPackage) that can be
 * updated via the Skunkcrafts Updater protocol.
 */
public interface SkunkcraftsUpdatable {

    /** Returns the display name of this addon. */
    String getName();

    /** Returns the current installed version of this addon, or null if unknown. */
    String getVersion();

    /** Returns the addon's base folder, where the Skunkcrafts config file lives. */
    Path getSkunkcraftsFolder();

    /** Returns the lazily-loaded Skunkcrafts config, or null if none. */
    SkunkcraftsConfig getSkunkcraftsConfig();

    /** Returns the lazily-fetched remote version, or null if not updatable. */
    String getSkunkcraftsLatestVersion();

    /** Returns the addon's beta channel config (skunkcrafts_updater_beta.cfg), or null if none. */
    default SkunkcraftsConfig getSkunkcraftsBetaConfig() {
        return SkunkcraftsUpdater.findBetaConfig(getSkunkcraftsFolder());
    }

    /**
     * Returns the content-truth availability of both channels plus the beta channel's latest label.
     * Domain classes cache this (see {@link SkunkcraftsUpdater#channelStatus(SkunkcraftsUpdatable)});
     * this default computes it live.
     */
    default SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
        return SkunkcraftsUpdater.channelStatus(this);
    }

    /**
     * Returns the latest available version (from the Skunkcrafts config by default,
     * or from a custom publication channel when overridden), or null if unknown.
     */
    default String getLatestVersion() {
        return getSkunkcraftsLatestVersion();
    }

    /** Whether this addon has a valid, enabled, non-locked Skunkcrafts config. */
    default boolean isSkunkcraftsUpdatable() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && !cfg.disabled() && !cfg.locked() && cfg.moduleUrl() != null;
    }

    /** Whether this addon is locked by the developer (updates temporarily unavailable). */
    default boolean isSkunkcraftsLocked() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && cfg.locked();
    }

    /**
     * Returns the version to compare against {@link #getLatestVersion()} when checking for updates:
     * the local cfg's {@code version} line (the protocol's own installed-version record),
     * falling back to {@link #getVersion()} (usually a binary heuristic) when the cfg has no version.
     */
    default String getInstalledVersion() {
        SkunkcraftsConfig cfg = getSkunkcraftsConfig();
        return cfg != null && cfg.version() != null ? cfg.version() : getVersion();
    }

    /**
     * Whether the stable channel has files to download. Availability is content truth (CRC diff),
     * never version labels; eligibility is checked live.
     */
    default boolean isSkunkcraftsUpdateAvailable() {
        return isSkunkcraftsUpdatable() && getSkunkcraftsChannelStatus().stableUpdateAvailable();
    }

    /** Whether the beta channel offers content distinct from stable. Eligibility is checked live. */
    default boolean isSkunkcraftsBetaUpdateAvailable() {
        return isSkunkcraftsUpdatable() && getSkunkcraftsChannelStatus().betaUpdateAvailable();
    }

    /** Whether any channel has files to download. Eligibility is checked live. */
    default boolean isAnySkunkcraftsUpdateAvailable() {
        return isSkunkcraftsUpdatable() && getSkunkcraftsChannelStatus().anyUpdateAvailable();
    }

    /** The beta channel's remote version label, or null when beta is not offered. */
    default String getSkunkcraftsBetaLatestVersion() {
        return getSkunkcraftsChannelStatus().betaLatestVersion();
    }

    /**
     * Fetches the remote whitelist and returns the files to update with their total download size.
     * @return the summary of files to update, or an empty summary if up to date, not updatable, or on error
     */
    default SkunkcraftsUpdateSummary getSkunkcraftsUpdateSummary() {
        if (!isSkunkcraftsUpdatable()) {
            return new SkunkcraftsUpdateSummary(0, 0);
        }
        return SkunkcraftsUpdater.computeFilesToUpdateSummary(getSkunkcraftsFolder(), getSkunkcraftsConfig());
    }

    /** Like {@link #getSkunkcraftsUpdateSummary()}, for the beta channel. */
    default SkunkcraftsUpdateSummary getSkunkcraftsBetaUpdateSummary() {
        SkunkcraftsConfig betaCfg = getSkunkcraftsBetaConfig();
        if (!isSkunkcraftsUpdatable() || betaCfg == null) {
            return new SkunkcraftsUpdateSummary(0, 0);
        }
        return SkunkcraftsUpdater.computeFilesToUpdateSummary(getSkunkcraftsFolder(), betaCfg);
    }

    /** Downloads and applies differential updates for this addon. */
    default void applySkunkcraftsUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException {
        if (!isSkunkcraftsUpdatable()) {
            throw new SkunkcraftsUpdateException(getClass().getSimpleName() + " is not Skunkcrafts-updatable");
        }
        SkunkcraftsUpdater.applyUpdate(getSkunkcraftsFolder(), getSkunkcraftsConfig(), progress);
    }

    /** Downloads and applies differential updates from the beta channel. */
    default void applySkunkcraftsBetaUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException {
        SkunkcraftsConfig betaCfg = getSkunkcraftsBetaConfig();
        if (!isSkunkcraftsUpdatable() || betaCfg == null) {
            throw new SkunkcraftsUpdateException(getClass().getSimpleName() + " has no Skunkcrafts beta channel");
        }
        SkunkcraftsUpdater.applyUpdate(getSkunkcraftsFolder(), betaCfg, progress);
    }
}
