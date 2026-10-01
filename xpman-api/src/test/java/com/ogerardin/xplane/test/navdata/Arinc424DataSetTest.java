package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.Arinc424DataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

/**
 * Tests the overriding flag and the US-only coverage warning of {@link Arinc424DataSet}.
 */
class Arinc424DataSetTest {

    private static final String FAA_HEADER =
            "HDR01FAACIFP18      001P013203969192610  09-SEP-202612:03:55  U.S.A. DOT FAA                \n" +
                    "HDR02                                 FEDERAL AVIATION ADMINISTRATION          \n";

    private static final String COMMERCIAL_HEADER =
            "HDR01NAVDAT48       001P013203969192610  09-SEP-202612:03:55  NAVDATA (AIRAC 424)            \n" +
                    "HDR02                                 GLOBAL NAVIGATION DATA                 \n";

    @TempDir
    Path xplaneRoot;

    @Test
    void simWideOverrideIsOverriding() throws Exception {
        Path data = write("earth_424.dat", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet("Sim-wide ARINC424 override", "",
                new XPlane(xplaneRoot), data, true, "earth_424.dat");

        assertThat(dataSet.isOverriding(), is(true));
    }

    @Test
    void legacyFaaApproachesSetIsNotOverriding() throws Exception {
        Path data = write("FAACIFP18", FAA_HEADER);

        Arinc424DataSet dataSet = new Arinc424DataSet("FAA updated approaches (legacy filename)", "",
                new XPlane(xplaneRoot), data, false, "FAACIFP18");

        assertThat(dataSet.isOverriding(), is(false));
    }

    @Test
    void warnsThatTheFaaDatasetCoversTheUsOnly() throws Exception {
        Path data = write("earth_424.dat", FAA_HEADER);
        Arinc424DataSet dataSet = new Arinc424DataSet("Sim-wide ARINC424 override", "",
                new XPlane(xplaneRoot), data, true, "earth_424.dat");

        InspectionResult result = dataSet.inspect();

        assertThat(result.getMessages(), hasItem(allOf(
                hasProperty("severity", is(Severity.WARN)),
                hasProperty("message", containsString("US-only coverage (FAACIFP18)")))));
    }

    @Test
    void makesNoCoverageClaimForACommercialPublisher() throws Exception {
        Path data = write("earth_424.dat", COMMERCIAL_HEADER);
        Arinc424DataSet dataSet = new Arinc424DataSet("Sim-wide ARINC424 override", "",
                new XPlane(xplaneRoot), data, true, "earth_424.dat");

        InspectionResult result = dataSet.inspect();

        assertThat(result.getMessages().stream()
                .map(InspectionMessage -> InspectionMessage.getMessage())
                .noneMatch(m -> m != null && m.contains("coverage")), is(true));
    }

    @Test
    void makesNoCoverageClaimWhenTheFileIsAbsent() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        Files.createDirectories(data);
        Arinc424DataSet dataSet = new Arinc424DataSet("Sim-wide ARINC424 override", "",
                new XPlane(xplaneRoot), data, true, "earth_424.dat");

        InspectionResult result = dataSet.inspect();

        assertThat(result.getMessages().stream()
                .map(InspectionMessage -> InspectionMessage.getMessage())
                .noneMatch(m -> m != null && m.contains("coverage")), is(true));
    }

    private Path write(String name, String contents) throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        Files.createDirectories(data);
        Files.writeString(data.resolve(name), contents);
        return data;
    }
}