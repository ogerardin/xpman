package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsChannelStatus;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsConfig;
import com.ogerardin.xplane.skunkcrafts.SkunkcraftsUpdatable;
import com.ogerardin.xpman.panels.plugins.UiFlyWithLuaScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests that the Plugins panel's script row reports the update of a Skunkcrafts-updatable
 * script (SGES): the update must show on the script row, not on the FlyWithLua plugin row.
 */
class UiFlyWithLuaScriptTest {

    @TempDir
    Path tempDir;

    @Test
    void updatableScriptRowReportsUpdateAndLatestVersion() throws Exception {
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript(),
                new SkunkcraftsChannelStatus(true, true, "Beta"));

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(true));
        assertThat(row.isBetaUpdateAvailable(), is(true));
        // the row's pill shows the stable channel's label; the wizard offers the beta channel too
        assertThat(row.getLatestVersion(), is("81.2"));
        assertThat(row.getUpdateTooltip(), is("Stable update: 81.2\nBeta update: Beta"));
    }

    @Test
    void betaOnlyUpdateStillShowsTheRowPill() throws Exception {
        // only the beta channel has content: the pill must still show (any channel)
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript(),
                new SkunkcraftsChannelStatus(false, true, "Beta"));

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(true));
        assertThat(row.isBetaUpdateAvailable(), is(true));
        assertThat(row.getUpdateTooltip(), is("Beta update: Beta"));
    }

    @Test
    void updatableScriptRowWithNoPendingContentReportsNoUpdate() throws Exception {
        // labels differ but no channel has content to download: no pill (the openSAM case)
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript(),
                SkunkcraftsChannelStatus.NONE);

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(false));
        assertThat(row.isBetaUpdateAvailable(), is(false));
    }

    @Test
    void plainScriptRowReportsNoUpdate() throws Exception {
        FlyWithLuaScript script = new FlyWithLuaScript(new XPlane(tempDir), createScript());

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(false));
        assertThat(row.isBetaUpdateAvailable(), is(false));
        assertThat(row.getLatestVersion(), is(nullValue()));
    }

    private Path createScript() throws IOException {
        Path luaFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts/test.lua");
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- test script\n");
        return luaFile;
    }

    /** A script stub that reports pending Skunkcrafts updates, as SGES does. */
    private static class UpdatableScript extends FlyWithLuaScript implements SkunkcraftsUpdatable {

        private final SkunkcraftsChannelStatus channelStatus;

        UpdatableScript(XPlane xPlane, Path luaFile, SkunkcraftsChannelStatus channelStatus) {
            super(xPlane, luaFile);
            this.channelStatus = channelStatus;
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
            return "81.2";
        }

        @Override
        public SkunkcraftsChannelStatus getSkunkcraftsChannelStatus() {
            return channelStatus;
        }
    }
}
