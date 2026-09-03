package com.ogerardin.xplane.test.scenery;

import com.ogerardin.test.util.EnableOnLocalXPlane;
import com.ogerardin.test.util.TimingExtension;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.exception.InvalidConfig;
import com.ogerardin.xplane.file.data.scenery.TokenSceneryPackIniItem;
import com.ogerardin.xplane.scenery.SceneryEntry;
import com.ogerardin.xplane.scenery.SceneryEntryStatus;
import com.ogerardin.xplane.scenery.SceneryManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Slf4j
@ExtendWith(TimingExtension.class)
@EnableOnLocalXPlane
class SceneryManagerTest {

    @Test
    void entriesShouldFollowIniOrder() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());
        SceneryManager manager = xPlane.getSceneryManager();
        manager.loadPackages();

        List<SceneryEntry> entries = manager.getSceneryEntries();
        assertFalse(entries.isEmpty());

        // ini-listed entries come first, in file order, with consecutive 1-based ranks
        List<SceneryEntry> iniEntries = entries.stream()
                .filter(e -> e.getIniItem() != null)
                .toList();
        assertFalse(iniEntries.isEmpty());
        for (int i = 0; i < iniEntries.size(); i++) {
            assertEquals(i + 1, iniEntries.get(i).getRank(), "rank of ini entry #" + (i + 1));
        }

        // entries not listed in the ini come last, with null rank
        List<SceneryEntry> notListed = entries.stream()
                .filter(e -> e.getIniItem() == null)
                .toList();
        assertThat(notListed, everyItem(hasProperty("rank", nullValue())));

        // *GLOBAL_AIRPORTS* resolves to the Global Airports folder when it exists on disk
        List<SceneryEntry> tokenEntries = iniEntries.stream()
                .filter(e -> e.getIniItem() instanceof TokenSceneryPackIniItem)
                .toList();
        if (!tokenEntries.isEmpty() && Files.isDirectory(xPlane.getPaths().globalAirports())) {
            assertThat(tokenEntries, everyItem(hasProperty("status", is(SceneryEntryStatus.IN_INI))));
        }

        entries.forEach(e -> log.info("{}: {} [{}] pkg={}", e.getRank(), e.getName(), e.getStatus(),
                e.getSceneryPackage() != null));
    }

    @Test
    void sceneryPackagesShouldExcludeUnresolvedEntries() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());
        var packages = xPlane.getSceneryManager().getSceneryPackages();
        assertFalse(packages.isEmpty());
        assertThat(packages, everyItem(notNullValue()));
    }

    @Test
    void moveToShouldReorderIniItemsAndUpdateRanks() throws InvalidConfig {
        XPlane xPlane = new XPlane(XPlaneTestUtil.getDefaultXPRootFolder());
        SceneryManager manager = xPlane.getSceneryManager();
        manager.loadPackages();

        List<SceneryEntry> iniEntries = manager.getSceneryEntries().stream()
                .filter(e -> e.getIniItem() != null)
                .toList();
        assumeTrue(iniEntries.size() >= 2, "needs at least 2 ini-listed entries");

        SceneryEntry first = iniEntries.get(0);
        SceneryEntry second = iniEntries.get(1);

        // moving the first entry to index 1 swaps it with the second
        assertTrue(manager.moveTo(first, 1));
        assertTrue(manager.isPendingChanges());

        List<SceneryEntry> reordered = manager.getSceneryEntries().stream()
                .filter(e -> e.getIniItem() != null)
                .toList();
        assertSame(second.getIniItem(), reordered.get(0).getIniItem());
        assertSame(first.getIniItem(), reordered.get(1).getIniItem());
        assertEquals(1, reordered.get(0).getRank());
        assertEquals(2, reordered.get(1).getRank());

        // same index, out-of-bounds indices, and not-listed entries are rejected
        assertFalse(manager.moveTo(reordered.get(0), 0));
        assertFalse(manager.moveTo(reordered.get(0), -1));
        assertFalse(manager.moveTo(reordered.get(0), reordered.size()));
        manager.getSceneryEntries().stream()
                .filter(e -> e.getIniItem() == null)
                .findFirst()
                .ifPresent(e -> assertFalse(manager.moveTo(e, 0)));
    }
}
