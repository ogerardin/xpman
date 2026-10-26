package com.ogerardin.xplane.skunkcrafts;

/**
 * Content-truth update availability per channel, plus the beta channel's latest version label.
 *
 * @param stableUpdateAvailable whether the stable channel has files to download
 * @param betaUpdateAvailable   whether the beta channel offers content distinct from stable
 * @param betaLatestVersion     the beta channel's remote version label, or null when beta is not offered
 */
public record SkunkcraftsChannelStatus(
        boolean stableUpdateAvailable,
        boolean betaUpdateAvailable,
        String betaLatestVersion
) {

    public static final SkunkcraftsChannelStatus NONE = new SkunkcraftsChannelStatus(false, false, null);

    public boolean anyUpdateAvailable() {
        return stableUpdateAvailable || betaUpdateAvailable;
    }
}
