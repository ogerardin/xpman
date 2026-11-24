package com.ogerardin.xplane.test.aircraft;

import com.ogerardin.test.util.EnableOnAircraftPresent;
import com.ogerardin.test.util.XPlaneTestUtil;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.custom.CalibraA330;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration test that exercises the {@link CalibraA330.MissingCardaEnginesInspection}
 * against the user's real X-Plane installation. Skipped when the Calibra A330-200
 * is not installed.
 */
@Slf4j
class CalibraA330MissingCardaEnginesLiveTest {

    private static final String AIRCRAFT_ACF = "Aircraft/Calibra A330-200/A330-200.acf";

    @Test
    @EnableOnAircraftPresent(AIRCRAFT_ACF)
    void detectsMissingCardaEnginesOnRealInstall() throws Exception {
        Path xpRoot = XPlaneTestUtil.getDefaultXPRootFolder();
        Path acfPath = xpRoot.resolve(AIRCRAFT_ACF);
        log.info("Calibra A330-200 present at {}", acfPath);

        XPlane xPlane = new XPlane(xpRoot);
        CalibraA330 aircraft = new CalibraA330(xPlane, new AcfFile(acfPath));

        InspectionResult result = CalibraA330.MissingCardaEnginesInspection.INSTANCE.inspect(aircraft);

        // All messages must be WARN (this is expected behavior, never blocks).
        assertThat(result.stream().map(InspectionMessage::getSeverity).toList(),
                everyItem(is(Severity.WARN)));

        // Each message is keyed by a livery folder name and carries two fixes: download link (no confirm) and remove livery (with confirm).
        for (InspectionMessage msg : result.getMessages()) {
            assertThat(msg.getObject(), startsWith("livery ["));
            assertThat(msg.getFixes().size(), is(2));
            assertThat(msg.getFixes().get(0).needsConfirmation(), is(false));
            assertThat(msg.getFixes().get(1).label(), is("remove livery"));
            assertThat(msg.getFixes().get(1).needsConfirmation(), is(true));
        }

        // The number of warnings matches the number of tagged liveries whose mod folder is
        // still placeholder-only (verified independently below).
        Path objectsDir = acfPath.resolveSibling("objects");
        Set<String> expectedLiveries = Files.list(acfPath.resolveSibling("liveries"))
                .filter(Files::isDirectory)
                .map(p -> "livery " + p.getFileName().toString())
                .filter(name -> {
                    var m = java.util.regex.Pattern.compile("\\[\\s*\\S*\\s*(GE|PW)\\s*]", java.util.regex.Pattern.CASE_INSENSITIVE)
                            .matcher(name);
                    if (!m.find()) return false;
                    String folderName = m.group(1).equals("GE") ? "GE Engines" : "PW Engines";
                    return isPlaceholderFolderPresent(objectsDir.resolve(folderName));
                })
                .collect(Collectors.toSet());

        Set<String> reportedLiveries = result.stream().map(InspectionMessage::getObject).collect(Collectors.toSet());
        assertThat(reportedLiveries, equalTo(expectedLiveries));
    }

    private static boolean isPlaceholderFolderPresent(Path modDir) {
        try (var stream = Files.list(modDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".obj"))
                    .anyMatch(p -> {
                        try {
                            return Files.readString(p).contains("CALIBRA_PLACEHOLDER");
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            return false;
        }
    }
}
