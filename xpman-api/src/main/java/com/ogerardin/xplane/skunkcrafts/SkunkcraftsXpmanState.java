package com.ogerardin.xplane.skunkcrafts;

import java.util.Map;

/**
 * XPman's persistent Skunkcrafts state for one addon, stored as
 * {@code skunkcrafts_updater_xpman.json} beside the addon's Skunkcrafts files.
 *
 * @param stale manifest entries proven stale (the repository's file list disagrees with the
 *              bytes it serves), keyed by channel base URL + relative path
 */
public record SkunkcraftsXpmanState(Map<String, StaleManifestEntry> stale) {

    /**
     * A manifest entry proven stale by a download verification failure: what the manifest
     * claimed, what the repository actually served, and the served file's HTTP validators
     * for conditional revalidation.
     */
    public record StaleManifestEntry(long manifestCrc, long servedCrc, String etag, String lastModified) {}
}
