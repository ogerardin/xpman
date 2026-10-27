package com.ogerardin.xpman.test;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xpman.util.jfx.SkunkcraftsUpdateUtil;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Pins update tooltip labels to the channels that actually offer content. */
class SkunkcraftsUpdateTooltipTest {

    @Test
    void availableVersionsOnlyListsOfferedChannels() {
        assertEquals("Stable 2.1", SkunkcraftsUpdateUtil.availableVersions(addon(true, false, "2.1", "2.2-b1")));
        assertEquals("Beta 2.2-b1", SkunkcraftsUpdateUtil.availableVersions(addon(false, true, "2.1", "2.2-b1")));
        assertEquals("Stable 2.1 · Beta 2.2-b1", SkunkcraftsUpdateUtil.availableVersions(addon(true, true, "2.1", "2.2-b1")));
        assertEquals("Beta ?", SkunkcraftsUpdateUtil.availableVersions(addon(false, true, "2.1", null)));
        assertEquals("", SkunkcraftsUpdateUtil.availableVersions(addon(false, false, "2.1", null)));
    }

    @Test
    void stableOnly() {
        assertEquals("Stable update: 5.4.1", tooltip(true, false, "5.4.1"));
    }

    @Test
    void betaOnly() {
        assertEquals("Beta update: 5.5.0-beta-3", tooltip(false, true, "5.4.1"));
    }

    @Test
    void bothChannels() {
        assertEquals("Stable update: 5.4.1\nBeta update: 5.5.0-beta-3", tooltip(true, true, "5.4.1"));
    }

    @Test
    void missingLabel() {
        assertEquals("Stable update: ?", tooltip(true, false, null));
    }

    @Test
    void noUpdates() {
        assertEquals("", tooltip(false, false, "5.4.1"));
    }

    private String tooltip(boolean stable, boolean beta, String latest) {
        return SkunkcraftsUpdateUtil.updateTooltip(addon(stable, beta, latest, "5.5.0-beta-3"));
    }

    private SkunkcraftsUpdatable addon(boolean stable, boolean beta, String latest, String betaLatest) {
        return new SkunkcraftsUpdatable() {
            @Override public String getName() { return "Addon"; }
            @Override public String getVersion() { return "5.4.0"; }
            @Override public Path getSkunkcraftsFolder() { return null; }
            @Override public SkunkcraftsConfig getSkunkcraftsConfig() {
                return new SkunkcraftsConfig("Addon", "5.4.0", "https://example.com", null, false, false, true);
            }
            @Override public String getSkunkcraftsLatestVersion() { return latest; }
            @Override public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
                return new SkunkcraftsChannelStatus(stable, beta, betaLatest);
            }
        };
    }
}
