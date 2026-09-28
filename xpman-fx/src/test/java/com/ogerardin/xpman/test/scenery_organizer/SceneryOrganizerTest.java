package com.ogerardin.xpman.test.scenery_organizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ogerardin.xpman.scenery_organizer.BuiltinSceneryClass;
import com.ogerardin.xpman.scenery_organizer.RegexSceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClass;
import com.ogerardin.xpman.scenery_organizer.SceneryClassesAdapter;
import com.ogerardin.xpman.scenery_organizer.SceneryOrganizer;
import com.ogerardin.xplane.scenery.SceneryPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SceneryOrganizerTest {

    @TempDir
    Path tempDir;

    private SceneryOrganizer organizer;

    @BeforeEach
    void setUp() {
        organizer = new SceneryOrganizer();
    }

    private SceneryPackage pkg(String name, boolean withAptDat, boolean withLibrary) throws IOException {
        Path folder = tempDir.resolve(name);
        Files.createDirectories(folder);
        if (withAptDat) {
            Path earthNav = folder.resolve("Earth nav data");
            Files.createDirectories(earthNav);
            Files.createFile(earthNav.resolve("apt.dat"));
        }
        if (withLibrary) {
            Files.createFile(folder.resolve("library.txt"));
        }
        return new SceneryPackage(folder);
    }

    @Test
    void airportWithLibraryIsClassifiedAsAirport() throws IOException {
        SceneryPackage pkg = pkg("my_airport", true, true);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result, is(BuiltinSceneryClass.AIRPORT));
    }

    @Test
    void pureLibraryIsClassifiedAsLibrary() throws IOException {
        SceneryPackage pkg = pkg("my_library", false, true);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result, is(BuiltinSceneryClass.LIBRARY));
    }

    @Test
    void landmarkWithAirportIsClassifiedAsLandmark() throws IOException {
        SceneryPackage pkg = pkg("X-Plane Landmarks - Dubai", true, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result.getName(), is("X-Plane Landmark"));
    }

    @Test
    void simheavenWithAirportIsClassifiedAsOverlay() throws IOException {
        SceneryPackage pkg = pkg("simheaven_x-world", true, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result.getName(), is("Overlay scenery"));
    }

    @Test
    void ortho4xpFolderIsClassifiedAsMesh() throws IOException {
        SceneryPackage pkg = pkg("Ortho4XP_+40-015", false, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result.getName(), is("Mesh scenery"));
    }

    @Test
    void simheavenFolderIsClassifiedAsOverlay() throws IOException {
        SceneryPackage pkg = pkg("simheaven_x-world", false, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result.getName(), is("Overlay scenery"));
    }

    @Test
    void zplusFolderIsClassifiedAsMesh() throws IOException {
        SceneryPackage pkg = pkg("z+orthophotos", false, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result.getName(), is("Mesh scenery"));
    }

    @Test
    void unknownFolderIsClassifiedAsOther() throws IOException {
        SceneryPackage pkg = pkg("random_folder", false, false);
        SceneryClass result = organizer.sceneryClass(pkg);
        assertThat(result, is(BuiltinSceneryClass.OTHER));
    }

    @Test
    void applySortsByListPosition() throws IOException {
        SceneryPackage library = pkg("lib", false, true);
        SceneryPackage airport = pkg("apt", true, false);
        SceneryPackage mesh = pkg("z+mesh", false, false);
        SceneryPackage unknown = pkg("unknown", false, false);

        List<SceneryPackage> result = organizer.apply(List.of(library, airport, mesh, unknown));

        assertThat(result.get(0), is(airport));
        assertThat(result.get(1), is(mesh));
        assertThat(result.get(2), is(library));
        assertThat(result.get(3), is(unknown));
    }

    @Test
    void nullListMigratesToDefaults() {
        SceneryOrganizer org = new SceneryOrganizer(null);
        List<SceneryClass> classes = org.getOrderedSceneryClasses();
        assertThat(classes, hasItem(BuiltinSceneryClass.AIRPORT));
        assertThat(classes, hasItem(BuiltinSceneryClass.LIBRARY));
        assertThat(classes, hasItem(BuiltinSceneryClass.OTHER));
    }

    @Test
    void emptyListMigratesToDefaults() {
        SceneryOrganizer org = new SceneryOrganizer(new ArrayList<>());
        List<SceneryClass> classes = org.getOrderedSceneryClasses();
        assertThat(classes, hasItem(BuiltinSceneryClass.AIRPORT));
        assertThat(classes, hasItem(BuiltinSceneryClass.LIBRARY));
        assertThat(classes, hasItem(BuiltinSceneryClass.OTHER));
    }

    @Test
    void oldFormatMigrationInsertsBuiltins() {
        List<SceneryClass> oldFormat = List.of(
                new RegexSceneryClass("Airport", ".*airport.*"),
                new RegexSceneryClass("Overlay", ".*overlay.*")
        );
        SceneryOrganizer org = new SceneryOrganizer(new ArrayList<>(oldFormat));
        List<SceneryClass> classes = org.getOrderedSceneryClasses();

        assertThat(classes.get(0), is(BuiltinSceneryClass.AIRPORT));
        assertThat(classes.get(1).getName(), is("Overlay"));
        assertThat(classes.get(classes.size() - 2), is(BuiltinSceneryClass.LIBRARY));
        assertThat(classes.get(classes.size() - 1), is(BuiltinSceneryClass.OTHER));
    }

    @Test
    void adapterRoundTrip() throws IOException {
        List<SceneryClass> original = List.of(
                BuiltinSceneryClass.AIRPORT,
                new RegexSceneryClass("Overlay", ".*overlay.*"),
                BuiltinSceneryClass.LIBRARY,
                BuiltinSceneryClass.OTHER
        );

        SceneryClassesAdapter adapter = new SceneryClassesAdapter();
        String json = adapter.toJson(original);
        List<SceneryClass> roundTripped = adapter.fromJson(json);

        assertThat(roundTripped, hasSize(original.size()));
        assertThat(roundTripped.get(0), is(BuiltinSceneryClass.AIRPORT));
        assertThat(roundTripped.get(1).getName(), is("Overlay"));
        assertThat(roundTripped.get(1).getRegex(), is(".*overlay.*"));
        assertThat(roundTripped.get(2), is(BuiltinSceneryClass.LIBRARY));
        assertThat(roundTripped.get(3), is(BuiltinSceneryClass.OTHER));
    }

    @Test
    void adapterHandlesOldFormatAllObjects() throws IOException {
        String oldJson = "[{\"name\":\"Airport\",\"regex\":\".*airport.*\"},{\"name\":\"Overlay\",\"regex\":\".*overlay.*\"}]";
        SceneryClassesAdapter adapter = new SceneryClassesAdapter();
        List<SceneryClass> result = adapter.fromJson(oldJson);

        assertThat(result, hasSize(2));
        assertThat(result.get(0), instanceOf(RegexSceneryClass.class));
        assertThat(result.get(0).getName(), is("Airport"));
        assertThat(result.get(1), instanceOf(RegexSceneryClass.class));
    }
}
