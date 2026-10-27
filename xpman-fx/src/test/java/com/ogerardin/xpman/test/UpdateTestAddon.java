package com.ogerardin.xpman.test;

import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdateSummary;

import java.nio.file.Path;

/** Supplies cached update offers and file summaries without network or disk access. */
public class UpdateTestAddon implements SkunkcraftsUpdatable {
    private final SkunkcraftsChannelStatus status;
    private final String stableVersion;

    public UpdateTestAddon(boolean stable, boolean beta) {
        this(stable, beta, "2.1", "2.2-b1");
    }

    public UpdateTestAddon(boolean stable, boolean beta, String stableVersion, String betaVersion) {
        this.status = new SkunkcraftsChannelStatus(stable, beta, betaVersion);
        this.stableVersion = stableVersion;
    }

    @Override public String getName() { return "Example addon"; }
    @Override public String getVersion() { return "binary heuristic"; }
    @Override public Path getSkunkcraftsFolder() { return null; }
    @Override public SkunkcraftsConfig getSkunkcraftsConfig() {
        return new SkunkcraftsConfig(getName(), "2.0", "https://example.com", null, false, false, true);
    }
    @Override public String getSkunkcraftsLatestVersion() { return stableVersion; }
    @Override public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() { return status; }
    @Override public SkunkcraftsUpdateSummary getSkunkcraftsUpdateSummary() { return new SkunkcraftsUpdateSummary(2, 100); }
    @Override public SkunkcraftsUpdateSummary getSkunkcraftsBetaUpdateSummary() { return new SkunkcraftsUpdateSummary(3, 200); }
}
