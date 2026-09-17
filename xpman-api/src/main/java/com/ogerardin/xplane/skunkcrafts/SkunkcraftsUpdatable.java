package com.ogerardin.xplane.skunkcrafts;

import java.io.IOException;

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

    /** Whether this addon has a valid, enabled, non-locked Skunkcrafts config. */
    boolean isSkunkcraftsUpdatable();

    /** Whether this addon is locked by the developer (updates temporarily unavailable). */
    boolean isSkunkcraftsLocked();

    /** Fetches the remote version from the Skunkcrafts module URL (lazy/cached). */
    String getSkunkcraftsLatestVersion();

    /** Whether a newer version is available remotely. */
    boolean isSkunkcraftsUpdateAvailable();

    /**
     * Returns the number of files that need to be updated.
     * This may involve network calls to fetch the remote whitelist.
     * @return the number of files to update, or 0 if up to date or not updatable
     */
    int getSkunkcraftsFilesToUpdateCount();

    /** Downloads and applies differential updates for this addon. */
    void applySkunkcraftsUpdate(ProgressListener progress) throws IOException, SkunkcraftsUpdateException;
}
