package com.ogerardin.xplane.skunkcrafts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

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

    /** Whether a newer version is available remotely. */
    default boolean isSkunkcraftsUpdateAvailable() {
        String latest = getSkunkcraftsLatestVersion();
        return latest != null && !Objects.equals(getVersion(), latest);
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

    /** Downloads and applies differential updates for this addon. */
    default void applySkunkcraftsUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException {
        if (!isSkunkcraftsUpdatable()) {
            throw new SkunkcraftsUpdateException(getClass().getSimpleName() + " is not Skunkcrafts-updatable");
        }
        SkunkcraftsUpdater.applyUpdate(getSkunkcraftsFolder(), getSkunkcraftsConfig(), progress);
    }
}
