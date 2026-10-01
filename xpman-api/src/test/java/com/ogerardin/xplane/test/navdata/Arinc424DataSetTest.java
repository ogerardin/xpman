package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.Arinc424DataSet;
import com.ogerardin.xplane.navdata.NavDataManager;
import com.ogerardin.xplane.navdata.NavDataSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Tests the role flag, the coverage warning and the AIRAC cycle consistency check of
 * {@link Arinc424DataSet}.
 */
class Arinc424DataSetTest {

    /** Header of an FAA ARINC 424 cycle, in the column layout the parser relies on. */
    private static final String FAA_HEADER =
            "HDR01FAACIFP18      001P013203969192610  09-SEP-202612:03:55  U.S.A. DOT FAA                \n" +
                    "HDR02                                 FEDERAL AVIATION ADMINISTRATION          \n" +
                    "HDR04                                 CODED INSTRUMENT FLIGHT PROCEDURES VOLUME 2610  EFFECTIVE 01 OCT 2026\n";

    private static final String COMMERCIAL_HEADER =
            "HDR01NAVDAT48       001P013203969192610  09-SEP-202612:03:55  NAVDATA (AIRAC 424)            \n" +
                    "HDR02                                 GLOBAL NAVIGATION DATA                 \n";

    /** X-Plane's own XPNAV header, which carries an explicit cycle. */
    private static final String XPNAV_HEADER = "I\n1200 Version - data cycle %s, build 20261001\n";

    /**
     * The two texts the cycle-consistency check can emit. Neither appears in the cycle
     * summary line, which would otherwise be matched too.
     */
    private static final Predicate<InspectionMessage> IS_CONSISTENCY_MESSAGE =
            message -> message.getMessage() != null
                    && (message.getMessage().contains("does not match")
                    || message.getMessage().contains("is not used"));

    @TempDir
    Path xplaneRoot;

    private XPlane xPlane;

    @BeforeEach
    void setUp() {
        // A real XPlane reads its version out of an app bundle; stub what the navdata
        // layers actually ask it for.
        xPlane = Mockito.mock(XPlane.class);
        when(xPlane.getBaseFolder()).thenReturn(xplaneRoot);
        when(xPlane.getMajorVersion()).thenReturn(XPlaneMajorVersion.XP12);
        when(xPlane.getPaths()).thenReturn(xPlane.new XplanePaths());
        when(xPlane.getNavDataManager()).thenReturn(new NavDataManager(xPlane));
    }

    @Test
    void simWideOverrideIsOverriding() throws Exception {
        Path data = writeCustomData("earth_424.dat", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet(NavDataManager.SIM_WIDE_OVERRIDE, "",
                xPlane, data, Arinc424DataSet.Role.SIM_WIDE_OVERRIDE, "earth_424.dat");

        assertThat(dataSet.isOverriding(), is(true));
    }

    @Test
    void faaApproachesSetIsNotOverriding() throws Exception {
        Path data = writeCustomData("FAACIFP18", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet(NavDataManager.FAA_APPROACHES, "",
                xPlane, data, Arinc424DataSet.Role.FAA_APPROACHES, "FAACIFP18");

        assertThat(dataSet.isOverriding(), is(false));
    }

    @Test
    void warnsThatTheFaaDatasetCoversTheUsOnly() throws Exception {
        Path data = writeCustomData("earth_424.dat", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet(NavDataManager.SIM_WIDE_OVERRIDE, "",
                xPlane, data, Arinc424DataSet.Role.SIM_WIDE_OVERRIDE, "earth_424.dat");

        assertThat(coverageMessages(dataSet.inspect()), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", containsString("US-only coverage (FAACIFP18)")))));
    }

    @Test
    void makesNoCoverageClaimForACommercialPublisher() throws Exception {
        Path data = writeCustomData("earth_424.dat", COMMERCIAL_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet(NavDataManager.SIM_WIDE_OVERRIDE, "",
                xPlane, data, Arinc424DataSet.Role.SIM_WIDE_OVERRIDE, "earth_424.dat");

        assertThat(coverageMessages(dataSet.inspect()), is(empty()));
    }

    @Test
    void reportsTheCycleFromTheArinc424Header() throws Exception {
        Path data = writeCustomData("earth_424.dat", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet(NavDataManager.SIM_WIDE_OVERRIDE, "",
                xPlane, data, Arinc424DataSet.Role.SIM_WIDE_OVERRIDE, "earth_424.dat");

        assertThat(dataSet.inspect().getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.INFO)),
                hasProperty("message", is("OK — cycle 2610")))));
    }

    @Test
    void warnsWhenTheApproachesCycleDiffersFromTheLayerItOverrides() throws Exception {
        writeCustomData("FAACIFP18", FAA_HEADER);
        writeDefaultData("earth_nav.dat", "2507");

        assertThat(consistencyMessages(approachesLayer()), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", containsString("AIRAC cycle 2507 does not match")))));
    }

    @Test
    void staysQuietWhenTheApproachesCycleMatchesTheLayerItOverrides() throws Exception {
        writeCustomData("FAACIFP18", FAA_HEADER);
        writeDefaultData("earth_nav.dat", "2610");

        assertThat(consistencyMessages(approachesLayer()), is(empty()));
    }

    @Test
    void prefersTheUpdatedBaseCycleOverTheShippedOne() throws Exception {
        writeCustomData("FAACIFP18", FAA_HEADER);
        writeDefaultData("earth_nav.dat", "1801");
        writeCustomData("earth_nav.dat", "2610");

        // the shipped base layer is shadowed by the updated base, and must not be reported
        // as a conflict just because it is older
        assertThat(consistencyMessages(approachesLayer()), is(empty()));
    }

    @Test
    void warnsThatTheApproachesLayerIsUnusedWhileTheOverrideIsInstalled() throws Exception {
        writeCustomData("earth_424.dat", FAA_HEADER);
        writeCustomData("FAACIFP18", FAA_HEADER);

        assertThat(consistencyMessages(approachesLayer()), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", containsString("is not used")))));
    }

    @Test
    void theSimWideOverrideNeverWarnsAboutACycleConflict() throws Exception {
        writeCustomData("earth_424.dat", FAA_HEADER);
        writeDefaultData("earth_nav.dat", "1801");

        assertThat(consistencyMessages(layerOf(NavDataManager.SIM_WIDE_OVERRIDE)), is(empty()));
    }

    @Test
    void staysQuietWhenNoApproachesFileIsInstalled() throws Exception {
        writeDefaultData("earth_nav.dat", "1801");

        assertThat(consistencyMessages(approachesLayer()), is(empty()));
    }

    private NavDataSet approachesLayer() {
        return layerOf(NavDataManager.FAA_APPROACHES);
    }

    private NavDataSet layerOf(String name) {
        return xPlane.getNavDataManager().getNavDataSets().stream()
                .filter(dataSet -> name.equals(dataSet.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no layer named " + name));
    }

    private static List<InspectionMessage> consistencyMessages(NavDataSet layer) {
        return layer.inspect().getMessages().stream().filter(IS_CONSISTENCY_MESSAGE).toList();
    }

    private static List<InspectionMessage> coverageMessages(InspectionResult result) {
        return result.getMessages().stream()
                .filter(message -> message.getMessage() != null
                        && message.getMessage().contains("coverage"))
                .toList();
    }

    private Path writeCustomData(String name, String contents) throws Exception {
        return write("Custom Data", name, contents);
    }

    private Path writeDefaultData(String name, String cycle) throws Exception {
        return write("Resources/default data", name, XPNAV_HEADER.formatted(cycle));
    }

    private Path write(String folder, String name, String contents) throws Exception {
        Path directory = xplaneRoot.resolve(folder);
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(name), contents);
        return directory;
    }
}