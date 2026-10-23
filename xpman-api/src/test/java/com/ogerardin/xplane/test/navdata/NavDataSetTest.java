package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.NavDataItem;
import com.ogerardin.xplane.navdata.NavDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;

/**
 * Hermetic tests for {@link NavDataSet#inspect()}: missing files and AIRAC cycle
 * consistency of the set's data files.
 */
class NavDataSetTest {

    private static final String DAT_WITH_CYCLE = "I\n1100 version - data cycle %s\n";

    /** Header of an FAA ARINC 424 cycle, in the column layout the parser relies on. */
    @SuppressWarnings("java:S6126") // Fixed-column fixture; text-block whitespace would shift parser columns.
    private static final String FAA_HEADER =
            "HDR01FAACIFP18      001P013203969192610  09-SEP-202612:03:55  U.S.A. DOT FAA                    \n" +
                    "HDR02                                 FEDERAL AVIATION ADMINISTRATION              \n";

    @TempDir
    Path xplaneRoot;

    private NavDataSet dataSet(Path dataFolder, String... fileNames) throws Exception {
        return new NavDataSet("Test set", "test", new XPlane(xplaneRoot), dataFolder, fileNames) {};
    }

    private static void writeDat(Path folder, String fileName, String cycle) throws Exception {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve(fileName), DAT_WITH_CYCLE.formatted(cycle));
    }

    private static void writeArinc424(Path folder, String fileName) throws Exception {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve(fileName), FAA_HEADER);
    }

    @Test
    void reportsMissingFilesAndCurrentCycle() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2004");

        InspectionResult result = dataSet(data, "earth_nav.dat", "earth_fix.dat").inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.INFO)),
                hasProperty("message", is("absent: Custom Data/earth_fix.dat")))));
        assertThat(result.getMessages(), hasItem(hasProperty("message",
                is("OK — cycle 2004"))));
    }

    @Test
    void warnsOnMixedCycles() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2004");
        writeDat(data, "earth_fix.dat", "1905");

        InspectionResult result = dataSet(data, "earth_nav.dat", "earth_fix.dat").inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", is("Mixed AIRAC cycles: 1905, 2004")))));
    }

    @Test
    void infoWhenNoData() throws Exception {
        InspectionResult result = dataSet(xplaneRoot.resolve("Custom Data")).inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.INFO)),
                hasProperty("message", is("No data present")))));
    }

    @Test
    void reportsPresentWhenCycleCannotBeRead() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeArinc424(data, "earth_424.dat");

        InspectionResult result = dataSet(data, "earth_424.dat").inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.INFO)),
                hasProperty("message", is("Present (AIRAC cycle unknown)")))));
    }

    @Test
    void consistentCycleIsTheOneSharedByTheExistingFiles() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2610");

        assertThat(dataSet(data, "earth_nav.dat", "earth_fix.dat").getConsistentCycle(),
                is(Optional.of("2610")));
    }

    @Test
    void consistentCycleIsEmptyWhenTheFilesDisagree() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2507");

        // a within-set conflict is reported by inspect() in its own right
        assertThat(dataSet(data, "earth_nav.dat", "earth_fix.dat").getConsistentCycle(), is(Optional.empty()));
    }

    @Test
    void consistentCycleIsEmptyWhenNoFileIsPresent() throws Exception {
        assertThat(dataSet(xplaneRoot.resolve("Custom Data"), "earth_nav.dat").getConsistentCycle(),
                is(Optional.empty()));
    }

    @Test
    void cyclesAreNormalisedToTheirAiracDesignator() {
        assertThat(NavDataItem.normalizeCycle("202610"), is("2610"));
        assertThat(NavDataItem.normalizeCycle("2610"), is("2610"));
        assertThat(NavDataItem.normalizeCycle(null), is(nullValue()));
    }

    @Test
    void stateIsAbsentWhenNoFileIsInstalled() throws Exception {
        assertThat(dataSet(xplaneRoot.resolve("Custom Data"), "earth_nav.dat").describeState(),
                is("Absent"));
    }

    @Test
    void stateIsNoDataWhenTheFileCarriesNoReadableCycle() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeArinc424(data, "earth_424.dat");

        assertThat(dataSet(data, "earth_424.dat").describeState(), is("No data"));
    }

    @Test
    void stateNamesTheSingleCycle() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2610");

        assertThat(dataSet(data, "earth_nav.dat", "earth_fix.dat").describeState(), is("Cycle 2610"));
    }

    @Test
    void stateIsMixedCyclesWhenTheFilesDisagree() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2507");

        assertThat(dataSet(data, "earth_nav.dat", "earth_fix.dat").describeState(), is("Mixed cycles"));
    }

    @Test
    void cyclesAreDistinctAndSorted() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2610");
        writeDat(data, "earth_awy.dat", "2507");

        assertThat(dataSet(data, "earth_nav.dat", "earth_fix.dat", "earth_awy.dat").getCycles(),
                is(List.of("2507", "2610")));
    }

    @Test
    void overridingIndexIsMinusOneForEmptyList() {
        assertThat(NavDataItem.overridingIndex(List.of()), is(-1));
    }

    @Test
    void overridingIndexIsMinusOneWhenNoLayerOverrides() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");

        assertThat(NavDataItem.overridingIndex(List.of(dataSet(data, "earth_nav.dat"))), is(-1));
    }

    @Test
    void overridingIndexIsFirstPresentOverridingLayer() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_424.dat", "2610");

        NavDataSet overrider = new NavDataSet("Override", "test", new XPlane(xplaneRoot), data, "earth_424.dat") {
            @Override
            public boolean isOverriding() {
                return true;
            }
        };

        assertThat(NavDataItem.overridingIndex(List.of(overrider)), is(1));
    }

    @Test
    void overridingIndexSkipsOverridingLayerWithNoFiles() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        NavDataSet overriderNoFiles = new NavDataSet("Override", "test", new XPlane(xplaneRoot), data) {
            @Override
            public boolean isOverriding() {
                return true;
            }
        };

        assertThat(NavDataItem.overridingIndex(List.of(overriderNoFiles)), is(-1));
    }

    @Test
    void activeReturnsEmptyForEmptyList() {
        assertThat(NavDataItem.active(List.of()), is(empty()));
    }

    @Test
    void activeReturnsEmptyWhenAllLayersAreAbsent() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        NavDataSet absent = dataSet(data, "missing.dat");
        assertThat(NavDataItem.active(List.of(absent)), is(empty()));
    }

    @Test
    void activeReturnsAllPresentLayersWhenNoOverrider() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2610");
        NavDataSet layer1 = dataSet(data, "earth_nav.dat");
        NavDataSet layer2 = dataSet(data, "earth_fix.dat");

        assertThat(NavDataItem.active(List.of(layer1, layer2)), containsInAnyOrder(layer1, layer2));
    }

    @Test
    void activeReturnsOnlyOverriderWhenPresent() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_424.dat", "2610");
        writeDat(data, "earth_nav.dat", "2610");

        NavDataSet overrider = new NavDataSet("Override", "test", new XPlane(xplaneRoot), data, "earth_424.dat") {
            @Override
            public boolean isOverriding() {
                return true;
            }
        };
        NavDataSet below = dataSet(data, "earth_nav.dat");

        assertThat(NavDataItem.active(List.of(overrider, below)), contains(overrider));
    }

    @Test
    void activeExcludesLayersAboveAPresentOverriderToo() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_424.dat", "2610");
        writeDat(data, "earth_nav.dat", "2610");

        NavDataSet above = dataSet(data, "earth_nav.dat");
        NavDataSet overrider = new NavDataSet("Override", "test", new XPlane(xplaneRoot), data, "earth_424.dat") {
            @Override
            public boolean isOverriding() {
                return true;
            }
        };

        assertThat(NavDataItem.active(List.of(above, overrider)), contains(overrider));
    }

    @Test
    void activeSkipsAbsentOverriderAndIncludesAllPresent() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2610");
        writeDat(data, "earth_fix.dat", "2610");

        NavDataSet absentOverrider = new NavDataSet("Override", "test", new XPlane(xplaneRoot), data, "missing.dat") {
            @Override
            public boolean isOverriding() {
                return true;
            }
        };
        NavDataSet present = dataSet(data, "earth_nav.dat");
        NavDataSet alsoPresent = dataSet(data, "earth_fix.dat");

        assertThat(NavDataItem.active(List.of(absentOverrider, present, alsoPresent)), containsInAnyOrder(present, alsoPresent));
    }
}
