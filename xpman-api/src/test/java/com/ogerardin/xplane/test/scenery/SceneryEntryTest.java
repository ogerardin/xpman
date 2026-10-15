package com.ogerardin.xplane.test.scenery;

import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.xplane.file.data.scenery.SceneryPackIniItem;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import com.ogerardin.xplane.scenery.SceneryPackage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
class SceneryEntryTest {

    private static SceneryPackage pkg(String path, boolean enabled) {
        var pkg = new SceneryPackage(Path.of(path));
        pkg.setEnabled(enabled);
        return pkg;
    }

    @Test
    void notListedEntryShouldBeEnabledWithoutRank() {
        var entry = SceneryEntry.notListed(pkg("/xplane/Custom Scenery/Foo", true));
        assertEquals(SceneryEntryStatus.NOT_LISTED, entry.getStatus());
        assertTrue(entry.isEnabled());
        assertNull(entry.getRank());
        assertNull(entry.getIniItem());
    }

    @Test
    void listedEnabledEntryShouldBeInIni() {
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo"),
                pkg("/xplane/Custom Scenery/Foo", true), 1);
        assertEquals(SceneryEntryStatus.IN_INI, entry.getStatus());
        assertTrue(entry.isEnabled());
        assertEquals(1, entry.getRank());
    }

    @Test
    void rankCanBeUpdatedAfterAnInMemoryMove() {
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo"), null, 2);
        entry.setRank(1);
        assertEquals(1, entry.getRank());
    }

    @Test
    void disabledInIniEntryShouldBeDisabled() {
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo", true),
                pkg("/xplane/Custom Scenery/Foo", true), 2);
        assertEquals(SceneryEntryStatus.IN_INI_DISABLED, entry.getStatus());
        assertFalse(entry.isEnabled());
    }

    @Test
    void latestVersionShouldComeFromSceneryPackage() {
        SceneryPackage pkg = new SceneryPackage(Path.of("/xplane/Custom Scenery/Foo")) {
            @Override public String getLatestVersion() { return "2.0"; }
        };

        assertEquals("2.0", SceneryEntry.notListed(pkg).getLatestVersion());
    }

    @Test
    void disabledPackageInspectionOmitsUpdateWarning() {
        SceneryPackage pkg = new SceneryPackage(Path.of("/xplane/Custom Scenery/Foo")) {
            @Override public String getVersion() { return "1.0"; }
            @Override public String getLatestVersion() { return "2.0"; }
        };
        pkg.setEnabled(false);

        assertTrue(pkg.inspect().stream().noneMatch(message -> message.getMessage().startsWith("Update available:")));
    }

    @Test
    void iniEnabledPackageInDisabledFolderShouldBeEnabled() {
        // The ini flag is authoritative; legacy folder placement is not.
        var entry = SceneryEntry.inIni(SceneryPackIniItem.of("Custom Scenery/Foo"),
                pkg("/xplane/Custom Scenery (disabled)/Foo", false), 1);
        assertEquals(SceneryEntryStatus.IN_INI, entry.getStatus());
    }

    @Test
    void unresolvedPathEntryShouldBeFolderMissing() {
        var entry = SceneryEntry.unresolved(SceneryPackIniItem.of("Custom Scenery/Gone"), 3);
        assertEquals(SceneryEntryStatus.FOLDER_MISSING, entry.getStatus());
        assertFalse(entry.isEnabled());
        assertEquals("Custom Scenery/Gone", entry.getName());
    }

    @Test
    void unresolvedTokenEntryShouldBeFolderMissing() {
        var entry = SceneryEntry.unresolved(SceneryPackIniItem.of("*GLOBAL_AIRPORTS*"), 1);
        assertEquals(SceneryEntryStatus.FOLDER_MISSING, entry.getStatus());
        assertEquals("*GLOBAL_AIRPORTS*", entry.getName());
    }

    @Test
    void tokenEntryShouldBeDetected() {
        var tokenItem = SceneryPackIniItem.of("*GLOBAL_AIRPORTS*");
        var entry = SceneryEntry.inIni(tokenItem, null, 1);
        assertTrue(entry.isToken());
    }

    @Test
    void pathEntryShouldNotBeToken() {
        var pathItem = SceneryPackIniItem.of("Custom Scenery/Test");
        var entry = SceneryEntry.inIni(pathItem, null, 1);
        assertFalse(entry.isToken());
    }

    @Test
    void unresolvedEntryAccessorsShouldBeNullSafe() {
        var entry = SceneryEntry.unresolved(SceneryPackIniItem.of("Custom Scenery/Gone"), 1);
        assertNull(entry.getVersion());
        assertNull(entry.getLatestVersion());
        assertNull(entry.getIconUrl());
        assertFalse(entry.getHasAirport());
        assertFalse(entry.isLibrary());
        assertNull(entry.getTileCount());
        assertNull(entry.getObjCount());
        assertTrue(entry.getLinks().isEmpty());
    }

    @Test
    void resolveFolderShouldResolvePathValueAgainstBaseFolder() {
        assertThat(SceneryEntry.resolveFolder(SceneryPackIniItem.of("Custom Scenery/KSEA"),
                        Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")),
                is(Path.of("/xplane/Custom Scenery/KSEA")));
    }

    @Test
    void resolveFolderShouldResolveGlobalAirportsToken() {
        assertThat(SceneryEntry.resolveFolder(SceneryPackIniItem.of(SceneryPackIniItem.GLOBAL_AIRPORTS_MARKER),
                        Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")),
                is(Path.of("/xplane/Global Scenery/Global Airports")));
    }

    @Test
    void resolveFolderShouldReturnNullForUnknownToken() {
        assertNull(SceneryEntry.resolveFolder(SceneryPackIniItem.of("*FOO*"),
                Path.of("/xplane"), Path.of("/xplane/Global Scenery/Global Airports")));
    }
}
