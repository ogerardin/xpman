package com.ogerardin.xplane.test.navdata;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.navdata.NavDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Hermetic tests for {@link NavDataSet#inspect()}: missing files and AIRAC cycle
 * consistency of the set's data files.
 */
class NavDataSetTest {

    private static final String DAT_WITH_CYCLE = "I\n1100 version - data cycle %s\n";

    @TempDir
    Path xplaneRoot;

    private NavDataSet dataSet(Path dataFolder, String... fileNames) throws Exception {
        return new NavDataSet("Test set", "test", new XPlane(xplaneRoot), dataFolder, fileNames) {};
    }

    private static void writeDat(Path folder, String fileName, String cycle) throws Exception {
        Files.createDirectories(folder);
        Files.writeString(folder.resolve(fileName), DAT_WITH_CYCLE.formatted(cycle));
    }

    @Test
    void reportsMissingFilesAndCurrentCycle() throws Exception {
        Path data = xplaneRoot.resolve("Custom Data");
        writeDat(data, "earth_nav.dat", "2004");

        InspectionResult result = dataSet(data, "earth_nav.dat", "earth_fix.dat").inspect();

        assertThat(result.getMessages(), hasItem(hasProperty("message",
                is("File not found: Custom Data/earth_fix.dat"))));
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
}
