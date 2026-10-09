package com.ogerardin.xplane.test.skunkcrafts;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateException;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkunkcraftsUpdatableTest {

    private static class FakeAddon implements SkunkcraftsUpdatable {
        private final String name;
        private final String version;
        private final SkunkcraftsConfig config;
        private final String latestVersion;

        FakeAddon(String name, String version, SkunkcraftsConfig config, String latestVersion) {
            this.name = name;
            this.version = version;
            this.config = config;
            this.latestVersion = latestVersion;
        }

        @Override public String getName() { return name; }
        @Override public String getVersion() { return version; }
        @Override public Path getSkunkcraftsFolder() { return Path.of("/fake"); }
        @Override public SkunkcraftsConfig getSkunkcraftsConfig() { return config; }
        @Override public String getSkunkcraftsLatestVersion() { return latestVersion; }
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
    void validConfigWithVersionMismatchMeansUpdateAvailable() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, false, false, true), "2.0");
        assertThat(addon.isSkunkcraftsUpdatable(), is(true));
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(true));
    }

    @Test
    void noUpdateAvailableWhenNoLongerUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, false, false, true), "2.0") {
            @Override
            public boolean isSkunkcraftsUpdatable() {
                return false;
            }
        };
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void validConfigWithSameVersionMeansNoUpdate() {
        FakeAddon addon = new FakeAddon("Test", "1.0",
                new SkunkcraftsConfig(null, null, "http://example.com", null, false, false, true), "1.0");
        assertThat(addon.isSkunkcraftsUpdatable(), is(true));
        assertThat(addon.isSkunkcraftsUpdateAvailable(), is(false));
    }

    @Test
    void applyUpdateThrowsWhenNotUpdatable() {
        FakeAddon addon = new FakeAddon("Test", "1.0", null, null);
        assertThrows(SkunkcraftsUpdateException.class, () -> addon.applySkunkcraftsUpdate((p, m) -> {}));
    }
}
