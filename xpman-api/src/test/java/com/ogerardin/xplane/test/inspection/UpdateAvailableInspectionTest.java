package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.UpdateAvailableInspection;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class UpdateAvailableInspectionTest {

    private SkunkcraftsUpdatable updatable(String name, String version, String latestVersion) {
        return updatable(name, version, latestVersion, SkunkcraftsChannelStatus.NONE);
    }

    private SkunkcraftsUpdatable updatable(String name, String version, String latestVersion,
                                           SkunkcraftsChannelStatus channelStatus) {
        return new SkunkcraftsUpdatable() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public Path getSkunkcraftsFolder() {
                return null;
            }

            @Override
            public SkunkcraftsConfig getSkunkcraftsConfig() {
                return new SkunkcraftsConfig(null, null, "https://example.com", null, false, false, true);
            }

            @Override
            public String getSkunkcraftsLatestVersion() {
                return latestVersion;
            }

            @Override
            public String getLatestVersion() {
                return latestVersion;
            }

            @Override
            public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
                return channelStatus;
            }
        };
    }

    @Test
    void upToDate() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", "1.0.0");
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void skunkcraftsTargetEmitsContentBasedMessages() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", "2.0.0",
                new SkunkcraftsChannelStatus(true, true, "Beta"));
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.size(), is(2));
        assertThat(result.get(0).getSeverity().name(), is("WARN"));
        assertThat(result.get(0).getObject(), is("MyAddon"));
        assertThat(result.get(0).getMessage(), is("Update available: 2.0.0"));
        assertThat(result.get(1).getMessage(), is("Beta update available: Beta"));
    }

    @Test
    void labelMismatchWithoutContentEmitsNothing() {
        // the openSAM Library case: labels differ, files identical
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", "2.0.0");
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void noLatestVersion() {
        SkunkcraftsUpdatable target = updatable("MyAddon", "1.0.0", null);
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void nonSkunkcraftsTargetKeepsVersionBasedWarning() {
        // XIvAp/Zibo scenario: a custom publication channel with no Skunkcrafts cfg
        SkunkcraftsUpdatable target = new SkunkcraftsUpdatable() {
            @Override public String getName() { return "MyAddon"; }
            @Override public String getVersion() { return "1.0.0"; }
            @Override public Path getSkunkcraftsFolder() { return null; }
            @Override public SkunkcraftsConfig getSkunkcraftsConfig() { return null; }
            @Override public String getSkunkcraftsLatestVersion() { return "2.0.0"; }
            @Override public String getLatestVersion() { return "2.0.0"; }
        };
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.size(), is(1));
        assertThat(result.get(0).getMessage(), is("Update available: 2.0.0"));
    }

    @Test
    void cfgVersionMatchesLatestButBinaryDiffersMeansNoWarn() {
        // SimLoad-Manager scenario: cfg says "1.0.0", latest says "1.0.0", but binary heuristic says "99.0.0"
        // Should NOT warn because cfg version (the authoritative source) matches latest
        SkunkcraftsUpdatable target = new SkunkcraftsUpdatable() {
            @Override public String getName() { return "MyAddon"; }
            @Override public String getVersion() { return "99.0.0"; } // binary heuristic (wrong)
            @Override public Path getSkunkcraftsFolder() { return null; }
            @Override public SkunkcraftsConfig getSkunkcraftsConfig() {
                return new SkunkcraftsConfig(null, "1.0.0", "https://example.com", null, false, false, true);
            }
            @Override public String getSkunkcraftsLatestVersion() { return "1.0.0"; }
            @Override public String getLatestVersion() { return "1.0.0"; }
            @Override public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() { return SkunkcraftsChannelStatus.NONE; }
        };
        InspectionResult result = UpdateAvailableInspection.INSTANCE.inspect(target);
        assertThat(result.isEmpty(), is(true));
    }
}
