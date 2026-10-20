package com.ogerardin.xpman.test;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
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
        FlyWithLuaScript script = new UpdatableScript(new XPlane(tempDir), createScript());

        UiFlyWithLuaScript row = new UiFlyWithLuaScript(script);

        assertThat(row.isUpdateAvailable(), is(true));
        assertThat(row.getLatestVersion(), is("81.2"));
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

    /** A script stub that reports a pending Skunkcrafts update, as SGES does. */
    private static class UpdatableScript extends FlyWithLuaScript implements SkunkcraftsUpdatable {

        UpdatableScript(XPlane xPlane, Path luaFile) {
            super(xPlane, luaFile);
        }

        @Override
        public Path getSkunkcraftsFolder() {
            return null;
        }

        @Override
        public SkunkcraftsConfig getSkunkcraftsConfig() {
            return null;
        }

        @Override
        public String getSkunkcraftsLatestVersion() {
            return "81.2";
        }

        @Override
        public boolean isSkunkcraftsUpdateAvailable() {
            return true;
        }
    }
}
