package com.ogerardin.xplane.test.scenery;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.scenery.SceneryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests scenery_packs.ini list management on a fake install: adding an on-disk folder above
 * anchor entries (openSAM's install flow) and removing a dangling entry after its folder was
 * trashed (openSAM's uninstall flow).
 */
class SceneryManagerIniTest {

    private static final String LIBRARY = "SCENERY_PACK Custom Scenery/openSAM_Library";
    private static final String GLOBAL_AIRPORTS = "SCENERY_PACK *GLOBAL_AIRPORTS*";

    @TempDir
    Path tempFolder;

    @Test
    void addsOnDiskFolderAboveGlobalAirports() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                "SCENERY_PACK Custom Scenery/Some Airport/",
                GLOBAL_AIRPORTS);
        createSceneryFolder(xPlane, "openSAM_Library");
        SceneryManager manager = xPlane.getSceneryManager();

        assertTrue(manager.addToIniAbove("openSAM_Library", "Global Airports", "SAM_Library"));

        List<String> lines = iniLines(xPlane);
        assertTrue(lines.indexOf(LIBRARY) < lines.indexOf(GLOBAL_AIRPORTS),
                "openSAM_Library above *GLOBAL_AIRPORTS*: " + lines);
    }

    @Test
    void insertsAboveTheFirstOfSeveralAnchors() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                "SCENERY_PACK Custom Scenery/Some Airport/",
                "SCENERY_PACK Custom Scenery/SAM_Library/",
                GLOBAL_AIRPORTS);
        createSceneryFolder(xPlane, "openSAM_Library");

        assertTrue(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports", "SAM_Library"));

        List<String> lines = iniLines(xPlane);
        assertTrue(lines.indexOf(LIBRARY) < lines.indexOf("SCENERY_PACK Custom Scenery/SAM_Library"),
                "openSAM_Library above SAM_Library: " + lines);
        assertTrue(lines.indexOf(LIBRARY) < lines.indexOf(GLOBAL_AIRPORTS),
                "openSAM_Library above *GLOBAL_AIRPORTS*: " + lines);
    }

    @Test
    void insertsAtTopWhenNoAnchorIsListed() throws Exception {
        XPlane xPlane = newXPlaneWithIni("SCENERY_PACK Custom Scenery/Some Airport/");
        createSceneryFolder(xPlane, "openSAM_Library");

        assertTrue(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports"));

        assertEquals(0, iniLines(xPlane).indexOf(LIBRARY), "openSAM_Library at top: " + iniLines(xPlane));
    }

    @Test
    void repositionsAnAlreadyListedFolderSittingTooLow() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                GLOBAL_AIRPORTS,
                "SCENERY_PACK Custom Scenery/openSAM_Library");  // listed, but below global airports
        createSceneryFolder(xPlane, "openSAM_Library");

        assertTrue(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports"));

        List<String> lines = iniLines(xPlane);
        assertTrue(lines.indexOf(LIBRARY) < lines.indexOf(GLOBAL_AIRPORTS),
                "openSAM_Library repositioned above *GLOBAL_AIRPORTS*: " + lines);
    }

    @Test
    void doesNothingWhenIniIsAbsent() throws Exception {
        XPlane xPlane = newXPlane();
        createSceneryFolder(xPlane, "openSAM_Library");

        assertFalse(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports"));
    }

    @Test
    void doesNothingWhenFolderIsNotOnDisk() throws Exception {
        XPlane xPlane = newXPlaneWithIni(GLOBAL_AIRPORTS);

        assertFalse(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports"));
        assertFalse(iniLines(xPlane).contains(LIBRARY));
    }

    @Test
    void removesDanglingEntryAfterFolderIsGone() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                "SCENERY_PACK Custom Scenery/Some Airport/",
                LIBRARY,           // dangling: no openSAM_Library folder on disk
                GLOBAL_AIRPORTS);

        assertTrue(xPlane.getSceneryManager().removeFromIni("openSAM_Library"));
        assertFalse(iniLines(xPlane).contains(LIBRARY));
    }

    @Test
    void keepsEntryWhenFolderIsStillOnDisk() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                LIBRARY,
                GLOBAL_AIRPORTS);
        createSceneryFolder(xPlane, "openSAM_Library");

        assertFalse(xPlane.getSceneryManager().removeFromIni("openSAM_Library"));
        assertTrue(iniLines(xPlane).contains(LIBRARY));
    }

    @Test
    void doesNotRewriteTheIniWhenTheFolderAlreadySitsAboveTheAnchors() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                LIBRARY, // already listed at the top
                "SCENERY_PACK Custom Scenery/Some Airport/",
                GLOBAL_AIRPORTS);
        createSceneryFolder(xPlane, "openSAM_Library");
        Path ini = xPlane.getPaths().customScenery().resolve("scenery_packs.ini");
        String before = Files.readString(ini);

        assertTrue(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports"));

        assertEquals(before, Files.readString(ini)); // no churn: position already correct
    }

    @Test
    void positionsAboveADisabledAnchor() throws Exception {
        XPlane xPlane = newXPlaneWithIni(
                "SCENERY_PACK Custom Scenery/Some Airport/",
                "SCENERY_PACK_DISABLED Custom Scenery/SAM_Library/",
                GLOBAL_AIRPORTS);
        createSceneryFolder(xPlane, "openSAM_Library");

        assertTrue(xPlane.getSceneryManager().addToIniAbove("openSAM_Library", "Global Airports", "SAM_Library"));

        List<String> lines = iniLines(xPlane);
        assertTrue(lines.indexOf(LIBRARY) < lines.indexOf("SCENERY_PACK_DISABLED Custom Scenery/SAM_Library"),
                "openSAM_Library above the disabled SAM_Library: " + lines);
    }

    @Test
    void removeFromIniRefreshesEntriesEvenWhenThereIsNoIni() throws Exception {
        XPlane xPlane = newXPlane();
        createSceneryFolder(xPlane, "openSAM_Library");
        SceneryManager manager = xPlane.getSceneryManager();
        assertTrue(hasEntryFor(manager, "openSAM_Library")); // the row shows before the folder is trashed

        deleteFolder(xPlane.getPaths().customScenery().resolve("openSAM_Library")); // what uninstall does
        boolean removed = manager.removeFromIni("openSAM_Library");

        assertFalse(removed); // nothing to remove without an ini
        assertFalse(hasEntryFor(manager, "openSAM_Library")); // but the row is refreshed away
    }

    // helpers

    private static boolean hasEntryFor(SceneryManager manager, String folderName) {
        return manager.getSceneryEntries().stream()
                .anyMatch(entry -> entry.getSceneryPackage() != null
                        && entry.getSceneryPackage().getFolder().getFileName().toString().equals(folderName));
    }

    private void deleteFolder(Path folder) throws IOException {
        try (var paths = Files.walk(folder)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        }
    }

    private XPlane newXPlane() throws Exception {
        return new XPlane(tempFolder);
    }

    private XPlane newXPlaneWithIni(String... entries) throws Exception {
        Path customScenery = tempFolder.resolve("Custom Scenery");
        Files.createDirectories(customScenery);
        Files.writeString(customScenery.resolve("scenery_packs.ini"),
                "I\n1000 Version\nSCENERY\n\n" + String.join("\n", entries) + "\n");
        return newXPlane();
    }

    private void createSceneryFolder(XPlane xPlane, String name) throws IOException {
        Path folder = xPlane.getPaths().customScenery().resolve(name);
        Files.createDirectories(folder);
        Files.writeString(folder.resolve("library.txt"), "EXPORT dummy");
    }

    private List<String> iniLines(XPlane xPlane) throws IOException {
        return Files.readAllLines(xPlane.getPaths().customScenery().resolve("scenery_packs.ini"))
                .stream().filter(line -> line.startsWith("SCENERY_PACK")).toList();
    }
}
