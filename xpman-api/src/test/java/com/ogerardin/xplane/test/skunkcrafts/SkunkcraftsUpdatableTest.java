package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannel;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdater;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkunkcraftsUpdatableTest {

    private static final SkunkcraftsConfig VALID_CFG =
            new SkunkcraftsConfig(null, null, "http://example.com", null, false, false, true);

    private static class FakeAddon implements SkunkcraftsUpdatable {
        private final String name;
        private final String version;
        private final SkunkcraftsConfig config;
        private final String latestVersion;
        private final SkunkcraftsChannelStatus channelStatus;

        FakeAddon(String name, String version, SkunkcraftsConfig config, String latestVersion) {
            this(name, version, config, latestVersion, SkunkcraftsChannelStatus.NONE);
        }

        FakeAddon(String name, String version, SkunkcraftsConfig config, String latestVersion,
                  SkunkcraftsChannelStatus channelStatus) {
            this.name = name;
            this.version = version;
            this.config = config;
            this.latestVersion = latestVersion;
            this.channelStatus = channelStatus;
        }

        @Override public String getName() { return name; }
        @Override public String getVersion() { return version; }
        @Override public Path getSkunkcraftsFolder() { return Path.of("/fake"); }
        @Override public SkunkcraftsConfig getSkunkcraftsConfig() { return config; }
        @Override public String getSkunkcraftsLatestVersion() { return latestVersion; }
        @Override public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() { return channelStatus; }
    }

    @Test
    void nullConfigMeansNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0", null, null);
        assertThat(addon.isSkunkcraftsUpdatable(), is(false));
        assertThat(addon.isSkunkcraftsLocked(), is(false));
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
        assertThat(addon.getSkunkcraftsUpdateSummary(), is(new SkunkcraftsUpdateSummary(0, 0)));
    }

    @Test
    void disabledConfigMeansNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, false, true, true), null);
        assertThat(addon.isSkunkcraftsUpdatable(), is(false));
    }

    @Test
    void lockedConfigMeansLockedAndNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, true, false, true), null);
        assertThat(addon.isSkunkcraftsLocked(), is(true));
        assertThat(addon.isSkunkcraftsUpdatable(), is(false));
    }

    @Test
    void noModuleUrlMeansNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, null, null, false, false, true), null);
        assertThat(addon.isSkunkcraftsUpdatable(), is(false));
    }

    @Test
    void stableStatusMeansUpdateAvailable() {
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(true, false, null));
        assertThat(addon.isSkunkcraftsUpdatable(), is(true));
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(true));
        assertThat(addon.isAnySkunkcraftsUpdateAvailable(), is(true));
    }

    @Test
    void labelMismatchWithoutContentIsNotAnUpdate() {
        // the openSAM Library case: remote version label bumped, files identical
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0", SkunkcraftsChannelStatus.NONE);
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
        assertThat(addon.isAnySkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void betaStatusDrivesBetaAvailability() {
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(false, true, "Beta"));
        assertThat(addon.isSkunkcraftsBetaUpdateAvailable(), is(true));
        assertThat(addon.getSkunkcraftsBetaLatestVersion(), is("Beta"));
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
        assertThat(addon.isAnySkunkcraftsUpdateAvailable(), is(true));
    }

    @Test
    void ignoreBetaChannelHidesBetaButKeepsStable() {
        FakeAddon bothChannels = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(true, true, "Beta"));
        SkunkcraftsUpdater.ignoreBetaChannel = () -> true;
        try {
            assertThat(bothChannels.isSkunkcraftsBetaUpdateAvailable(), is(false));
            assertThat(bothChannels.isAnySkunkcraftsUpdateAvailable(), is(true));
        } finally {
            SkunkcraftsUpdater.ignoreBetaChannel = () -> false;
        }
    }

    @Test
    void ignoreBetaChannelHidesBetaOnlyUpdates() {
        FakeAddon betaOnly = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(false, true, "Beta"));
        SkunkcraftsUpdater.ignoreBetaChannel = () -> true;
        try {
            assertThat(betaOnly.isAnySkunkcraftsUpdateAvailable(), is(false));
            assertThat(SkunkcraftsChannel.BETA.hasUpdate(betaOnly), is(false));
        } finally {
            SkunkcraftsUpdater.ignoreBetaChannel = () -> false;
        }
    }

    @Test
    void noUpdateAvailableWhenNoLongerUpdatable() {
        // eligibility is checked live, not baked into the cached status
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(true, false, null)) {
            @Override
            public boolean isSkunkcraftsUpdatable() {
                return false;
            }
        };
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
        assertThat(addon.isSkunkcraftsBetaUpdateAvailable(), is(false));
        assertThat(addon.isAnySkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void disabledAndLockedAddonsHaveNoChannelStatus() {
        FakeAddon disabled = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, false, true, true), "2.0") {
            @Override
            public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
                return SkunkcraftsUpdater.channelStatus(this);
            }
        };
        assertThat(disabled.getSkunkcraftsChannelStatus(), is(SkunkcraftsChannelStatus.NONE));
        assertThat(disabled.isSkunkcraftsUpdateAvailable(), is(false));

        FakeAddon locked = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, true, false, true), "2.0") {
            @Override
            public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
                return SkunkcraftsUpdater.channelStatus(this);
            }
        };
        assertThat(locked.getSkunkcraftsChannelStatus(), is(SkunkcraftsChannelStatus.NONE));
    }

    @Test
    void resolveReturnsRequestedWhenItHasAnUpdate() {
        FakeAddon bothChannels = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(true, true, "Beta"));
        assertThat(SkunkcraftsChannel.resolve(SkunkcraftsChannel.STABLE, bothChannels), is(SkunkcraftsChannel.STABLE));
        assertThat(SkunkcraftsChannel.resolve(SkunkcraftsChannel.BETA, bothChannels), is(SkunkcraftsChannel.BETA));
    }

    @Test
    void resolveFallsBackToSoleAvailableChannel() {
        FakeAddon betaOnly = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(false, true, "Beta"));
        assertThat(SkunkcraftsChannel.resolve(SkunkcraftsChannel.STABLE, betaOnly), is(SkunkcraftsChannel.BETA));

        FakeAddon stableOnly = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(true, false, null));
        assertThat(SkunkcraftsChannel.resolve(SkunkcraftsChannel.BETA, stableOnly), is(SkunkcraftsChannel.STABLE));
    }

    @Test
    void resolveReturnsRequestedWhenNeitherHasUpdate() {
        FakeAddon none = new FakeAddon("Test", "1.0", VALID_CFG, "2.0", SkunkcraftsChannelStatus.NONE);
        assertThat(SkunkcraftsChannel.resolve(SkunkcraftsChannel.BETA, none), is(SkunkcraftsChannel.BETA));
    }

    /** SimLoad Manager scenario: cfg has its own version — use it, ignore the binary heuristic. */
    @Test
    void cfgVersionUsedAsInstalledVersion() {
        FakeAddon addon = new FakeAddon("Test", "99.0",
                new SkunkcraftsConfig(null, "5.0.6", "http://example.com", null, false, false, true), "5.0.6");
        assertThat(addon.getInstalledVersion(), is("5.0.6"));
    }

    @Test
    void getInstalledVersionFallsBackToGetVersionWhenCfgNull() {
        FakeAddon addon = new FakeAddon("Test", "1.0", null, "2.0");
        assertThat(addon.getInstalledVersion(), is("1.0"));
    }

    @Test
    void getInstalledVersionFallsBackToGetVersionWhenCfgVersionNull() {
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0");
        assertThat(addon.getInstalledVersion(), is("1.0"));
    }

    @Test
    void applyUpdateThrowsWhenNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0", null, null);
        assertThrows(SkunkcraftsUpdateException.class, () -> addon.applySkunkcraftsUpdate((p, m) -> {}));
    }

    @Test
    void applyBetaUpdateThrowsWithoutBetaChannel() {
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(false, true, "Beta"));
        assertThrows(SkunkcraftsUpdateException.class, () -> addon.applySkunkcraftsBetaUpdate((p, m) -> {}));
    }

    @Test
    void betaSummaryIsEmptyWithoutBetaCfg() {
        FakeAddon addon = new FakeAddon("Test", "1.0", VALID_CFG, "2.0",
                new SkunkcraftsChannelStatus(false, true, "Beta"));
        assertThat(addon.getSkunkcraftsBetaUpdateSummary(), is(new SkunkcraftsUpdateSummary(0, 0)));
    }
}
