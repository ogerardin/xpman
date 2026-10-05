package com.ogerardin.xplane.test.lua;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLua;
import com.ogerardin.xplane.plugins.custom.lua.FlyWithLuaScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FlyWithLuaScriptTest {

    @TempDir
    Path tempDir;

    private XPlane newXPlane() throws Exception {
        return new XPlane(tempDir);
    }

    private Path createScript(String folderName, String fileName) throws IOException {
        Path folder = tempDir.resolve("Resources/plugins/FlyWithLua/" + folderName);
        Files.createDirectories(folder);
        Path luaFile = folder.resolve(fileName);
        Files.writeString(luaFile, "-- test script\n");
        return luaFile;
    }

    @Test
    void scriptInScriptsFolderIsEnabled() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        assertThat(script.isEnabled(), is(true));
    }

    @Test
    void disableMovesFileToDisabledFolder() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        script.setEnabled(false);

        Path disabledFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts (disabled)/test.lua");
        assertThat(Files.exists(disabledFile), is(true));
        assertThat(Files.exists(luaFile), is(false));

        FlyWithLuaScript reloaded = new FlyWithLuaScript(newXPlane(), disabledFile);
        assertThat(reloaded.isEnabled(), is(false));
    }

    @Test
    void enableMovesFileBackToScriptsFolder() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);
        script.setEnabled(false);

        Path disabledFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts (disabled)/test.lua");
        FlyWithLuaScript disabled = new FlyWithLuaScript(newXPlane(), disabledFile);
        disabled.setEnabled(true);

        assertThat(Files.exists(luaFile), is(true));
        assertThat(Files.exists(disabledFile), is(false));

        FlyWithLuaScript reloaded = new FlyWithLuaScript(newXPlane(), luaFile);
        assertThat(reloaded.isEnabled(), is(true));
    }

    @Test
    void setEnabledIsIdempotent() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        script.setEnabled(true);
        assertThat(Files.exists(luaFile), is(true));
    }

    @Test
    void disableOverwritesStalePeer() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        Files.writeString(luaFile, "-- fresh\n");

        Path disabledFolder = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts (disabled)");
        Files.createDirectories(disabledFolder);
        Path stale = disabledFolder.resolve("test.lua");
        Files.writeString(stale, "-- stale\n");

        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);
        script.setEnabled(false);

        assertThat(Files.readString(stale), is("-- fresh\n"));
    }

    @Test
    void flyWithLuaGetScriptsReturnsBothEnabledAndDisabled() throws Exception {
        createScript("Scripts", "enabled.lua");
        createScript("Scripts (disabled)", "disabled.lua");

        Path xplFolder = tempDir.resolve("Resources/plugins/FlyWithLua/64");
        Files.createDirectories(xplFolder);
        Path xplFile = xplFolder.resolve("FlyWithLua.xpl");
        Files.writeString(xplFile, "");

        XPlane xPlane = newXPlane();
        FlyWithLua flyWithLua = new FlyWithLua(xPlane, xplFile);
        List<FlyWithLuaScript> scripts = flyWithLua.getScripts();

        assertThat(scripts, hasSize(2));
        assertThat(scripts.stream().filter(FlyWithLuaScript::isEnabled).count(), is(1L));
        assertThat(scripts.stream().filter(s -> !s.isEnabled()).count(), is(1L));
    }

    @Test
    void quarantinedScriptIsDetected() throws Exception {
        Path luaFile = createScript("Scripts (Quarantine)", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        assertThat(script.isQuarantined(), is(true));
        assertThat(script.isEnabled(), is(false));
        assertThat(script.isDisabled(), is(false));
    }

    @Test
    void quarantineMovesFileToQuarantineFolder() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        script.setQuarantined(true);

        Path quarantinedFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts (Quarantine)/test.lua");
        assertThat(Files.exists(quarantinedFile), is(true));
        assertThat(Files.exists(luaFile), is(false));

        FlyWithLuaScript reloaded = new FlyWithLuaScript(newXPlane(), quarantinedFile);
        assertThat(reloaded.isQuarantined(), is(true));
        assertThat(reloaded.isEnabled(), is(false));
    }

    @Test
    void unquarantineMovesFileBackToScriptsFolder() throws Exception {
        Path luaFile = createScript("Scripts", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);
        script.setQuarantined(true);

        Path quarantinedFile = tempDir.resolve("Resources/plugins/FlyWithLua/Scripts (Quarantine)/test.lua");
        FlyWithLuaScript quarantined = new FlyWithLuaScript(newXPlane(), quarantinedFile);
        quarantined.setQuarantined(false);

        assertThat(Files.exists(luaFile), is(true));
        assertThat(Files.exists(quarantinedFile), is(false));

        FlyWithLuaScript reloaded = new FlyWithLuaScript(newXPlane(), luaFile);
        assertThat(reloaded.isEnabled(), is(true));
        assertThat(reloaded.isQuarantined(), is(false));
    }

    @Test
    void setQuarantinedIsIdempotent() throws Exception {
        Path luaFile = createScript("Scripts (Quarantine)", "test.lua");
        FlyWithLuaScript script = new FlyWithLuaScript(newXPlane(), luaFile);

        script.setQuarantined(true);
        assertThat(Files.exists(luaFile), is(true));
    }

    @Test
    void flyWithLuaGetScriptsReturnsAllThreeFolders() throws Exception {
        createScript("Scripts", "enabled.lua");
        createScript("Scripts (disabled)", "disabled.lua");
        createScript("Scripts (Quarantine)", "quarantined.lua");

        Path xplFolder = tempDir.resolve("Resources/plugins/FlyWithLua/64");
        Files.createDirectories(xplFolder);
        Path xplFile = xplFolder.resolve("FlyWithLua.xpl");
        Files.writeString(xplFile, "");

        XPlane xPlane = newXPlane();
        FlyWithLua flyWithLua = new FlyWithLua(xPlane, xplFile);
        List<FlyWithLuaScript> scripts = flyWithLua.getScripts();

        assertThat(scripts, hasSize(3));
        assertThat(scripts.stream().filter(FlyWithLuaScript::isEnabled).count(), is(1L));
        assertThat(scripts.stream().filter(FlyWithLuaScript::isDisabled).count(), is(1L));
        assertThat(scripts.stream().filter(FlyWithLuaScript::isQuarantined).count(), is(1L));
    }
}
