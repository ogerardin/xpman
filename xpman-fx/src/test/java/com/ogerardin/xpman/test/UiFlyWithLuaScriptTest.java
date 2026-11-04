package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import com.ogerardin.xplane.update.*;
import java.util.Optional;
import java.util.List;
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

    @Test
    void scriptMenuExposesInspection() {
        assertThat(com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper.computeRelevantMethods(UiFlyWithLuaScript.class)
                .stream().anyMatch(method -> method.getName().equals("inspect")), is(true));
    }

    @TempDir
    Path tempDir;

    @Test
    void updatableScriptRowReportsUpdateAndLatestVersion() throws Exception {
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript(),
                UpdateChannel.RELEASE, UpdateChannel.BETA);

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(true));
        // the row's pill shows the stable channel's label; the wizard offers the beta channel too
        assertThat(row.getLatestVersion(), is("81.2"));
        assertThat(row.getUpdateTooltip(), is("Stable update: 81.2\nBeta update: Beta"));
    }

    @Test
    void betaOnlyUpdateStillShowsTheRowPill() throws Exception {
        // only the beta channel has content: the pill must still show (any channel)
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript(),
                UpdateChannel.BETA);

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(true));
        assertThat(row.getUpdateTooltip(), is("Beta update: Beta"));
    }

    @Test
    void updatableScriptRowWithNoPendingContentReportsNoUpdate() throws Exception {
        // labels differ but no channel has content to download: no pill (the openSAM case)
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript());

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(false));
    }

    @Test
    void plainScriptRowReportsNoUpdate() throws Exception {
        FlyWithLuaScript script = new FlyWithLuaScript(new XPlane(tempDir), createScript());

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(false));
        assertThat(row.getLatestVersion(), is(nullValue()));
    }

    private Path createScript() throws IOException {
        Path luaFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts/test.lua");
        Files.createDirectories(luaFile.getParent());
        Files.writeString(luaFile, "-- test script\n");
        return luaFile;
    }

    /** A script stub that reports pending Skunkcrafts updates, as SGES does. */
    private static class UpdatableScript extends FlyWithLuaScript implements Updatable {

        private final List<UpdateChannel> channels;

        UpdatableScript(XPlane xPlane, Path luaFile, UpdateChannel... channels) {
            super(xPlane, luaFile);
            this.channels = List.of(channels);
        }

        @Override
        public Optional<UpdateProvider> getUpdateProvider() {
            var offers = channels.stream().map(channel -> new UpdateOffer(channel,
                    channel == UpdateChannel.RELEASE ? "81.2" : "Beta", (UpdateAction.Direct) p -> {})).toList();
            return Optional.of(new UpdateProvider() {
                @Override public String getName() { return "Test"; }
                /** No-op: this fixture serves a precomputed status. */
                @Override public void checkForUpdates() { /* fixture serves a precomputed status */ }
                @Override public UpdateStatus getStatus() {
                    return new UpdateStatus(offers.isEmpty() ? UpdateStatus.State.CURRENT : UpdateStatus.State.AVAILABLE,
                            "80.1", "81.2", offers, null);
                }
            });
        }
    }
}
