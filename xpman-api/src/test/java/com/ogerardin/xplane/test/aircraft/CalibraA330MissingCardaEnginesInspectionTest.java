package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.aircraft.Livery;
import com.ogerardin.xplane.aircraft.custom.CalibraA330;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class CalibraA330MissingCardaEnginesInspectionTest {

    @TempDir
    Path tempDir;

    private static final String PLACEHOLDER_OBJ =
            "I\n800\nOBJ\n\n# CALIBRA_PLACEHOLDER: empty stand-in.\n\nPOINT_COUNTS\t0\t0\t0\t0\n";

    @Test
    void emitsOneMessagePerLiveryRequiringMissingMod() throws IOException {
        Path geDir = tempDir.resolve("objects/GE Engines");
        Files.createDirectories(geDir);
        Files.writeString(geDir.resolve("GE_L_Engine.obj"), PLACEHOLDER_OBJ);

        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 GE] Air France F-GZCL"));
        Files.createDirectories(liveriesDir.resolve("[A332 RR] CalibraSim House"));

        Aircraft aircraft = mockAircraft(tempDir, List.of("[A332 GE] Air France F-GZCL", "[A332 RR] CalibraSim House"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(1));
        InspectionMessage msg = result.get(0);
        assertThat(msg.getSeverity(), is(Severity.WARN));
        assertThat(msg.getObject(), is("livery [A332 GE] Air France F-GZCL"));
        assertThat(msg.getMessage(), is("requires the Carda GE CF6-80E1 engine mod, which is not installed"));
        assertThat(msg.getDetails(),
                is("Without Carda's GE CF6-80E1 engine mod, this livery shows a "
                        + "'CARDA MOD MISSING' box for 30 seconds in the sim. "
                        + "To use this livery, download and install the mod."));
    }

    @Test
    void attachesDownloadFixForTheCorrectMod() throws IOException {
        Path pwDir = tempDir.resolve("objects/PW Engines");
        Files.createDirectories(pwDir);
        Files.writeString(pwDir.resolve("pw_l_engines.obj"), PLACEHOLDER_OBJ);

        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 PW] SWISS HB-IQA"));

        Aircraft aircraft = mockAircraft(tempDir, List.of("[A332 PW] SWISS HB-IQA"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(1));
        InspectionMessage msg = result.get(0);
        assertThat(msg.getFixes().size(), is(1));
        assertThat(msg.getFixes().get(0).label(),
                is("open Carda PW4000 download page (requires login)"));
        assertThat(msg.getFixes().get(0).needsConfirmation(), is(false));
    }

    @Test
    void noWarningWhenEngineModIsInstalled() throws IOException {
        Path geDir = tempDir.resolve("objects/GE Engines");
        Files.createDirectories(geDir);
        Files.writeString(geDir.resolve("GE_L_Engine.obj"), "I\n800\nOBJ\nVT 1.0 2.0 3.0\n");

        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 GE] Some Livery"));

        Aircraft aircraft = mockAircraft(tempDir, List.of("[A332 GE] Some Livery"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(0));
    }

    @Test
    void noWarningWhenNoLiveryRequiresTheMissingMod() throws IOException {
        Path geDir = tempDir.resolve("objects/GE Engines");
        Files.createDirectories(geDir);
        Files.writeString(geDir.resolve("GE_L_Engine.obj"), PLACEHOLDER_OBJ);

        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 RR] Some Livery"));

        Aircraft aircraft = mockAircraft(tempDir, List.of("[A332 RR] Some Livery"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(0));
    }

    @Test
    void emitsSeparateMessageForEachAffectedLivery() throws IOException {
        Path geDir = tempDir.resolve("objects/GE Engines");
        Files.createDirectories(geDir);
        Files.writeString(geDir.resolve("GE_L_Engine.obj"), PLACEHOLDER_OBJ);

        Path pwDir = tempDir.resolve("objects/PW Engines");
        Files.createDirectories(pwDir);
        Files.writeString(pwDir.resolve("pw_l_engines.obj"), PLACEHOLDER_OBJ);

        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 GE] Air France F-GZCL"));
        Files.createDirectories(liveriesDir.resolve("[A332 GE] British Airways"));
        Files.createDirectories(liveriesDir.resolve("[A332 PW] SWISS HB-IQA"));
        Files.createDirectories(liveriesDir.resolve("[A332 RR] CalibraSim House"));

        Aircraft aircraft = mockAircraft(tempDir, List.of(
                "[A332 GE] Air France F-GZCL",
                "[A332 GE] British Airways",
                "[A332 PW] SWISS HB-IQA",
                "[A332 RR] CalibraSim House"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(3));
        List<String> objects = result.stream().map(InspectionMessage::getObject).toList();
        assertThat(objects, containsInAnyOrder(
                "livery [A332 GE] Air France F-GZCL",
                "livery [A332 GE] British Airways",
                "livery [A332 PW] SWISS HB-IQA"));

        // Each GE livery references the GE fix; the PW livery references the PW fix.
        for (InspectionMessage msg : result.getMessages()) {
            assertThat(msg.getFixes().size(), is(1));
            if (msg.getObject().contains("GE")) {
                assertThat(msg.getMessage(), containsString("GE CF6-80E1"));
                assertThat(msg.getFixes().get(0).label(), containsString("GE CF6-80E1"));
            } else {
                assertThat(msg.getMessage(), containsString("PW4000"));
                assertThat(msg.getFixes().get(0).label(), containsString("PW4000"));
            }
        }
    }

    @Test
    void missingFolderIsTreatedAsMissingMod() throws IOException {
        Path liveriesDir = tempDir.resolve("liveries");
        Files.createDirectories(liveriesDir.resolve("[A332 GE] Some Livery"));

        Aircraft aircraft = mockAircraft(tempDir, List.of("[A332 GE] Some Livery"));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getObject(), is("livery [A332 GE] Some Livery"));
    }

    private static Aircraft mockAircraft(Path aircraftFolder, List<String> liveryFolderNames) {
        AcfFile acfFile = mock(AcfFile.class);
        when(acfFile.getFile()).thenReturn(aircraftFolder.resolve("A330-200.acf"));

        List<Livery> liveries = liveryFolderNames.stream()
                .map(name -> {
                    Livery livery = mock(Livery.class);
                    when(livery.getName()).thenReturn(name);
                    return livery;
                })
                .toList();

        Aircraft aircraft = mock(Aircraft.class);
        when(aircraft.getAcfFile()).thenReturn(acfFile);
        when(aircraft.getLiveries()).thenReturn(liveries);
        return aircraft;
    }
}
