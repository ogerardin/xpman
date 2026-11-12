package com.ogerardin.xplane.test.inspection;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.inspection.impl.StartLogInspection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class StartLogInspectionTest {

    @TempDir
    Path tempDir;

    @Test
    void missingLogFileYieldsSingleError() throws Exception {
        List<InspectionMessage> messages = inspect();

        assertThat(messages, hasSize(1));
        assertThat(messages.getFirst().getSeverity(), is(Severity.ERROR));
        assertThat(messages.getFirst().getMessage(), containsString("Log.txt not found"));
    }

    @Test
    void emptyLogYieldsWarn() throws Exception {
        Files.createFile(tempDir.resolve("Log.txt"));

        List<InspectionMessage> messages = inspect();

        assertThat(messages, hasSize(1));
        assertThat(messages.getFirst().getSeverity(), is(Severity.WARN));
        assertThat(messages.getFirst().getMessage(), containsString("empty"));
    }

    @Test
    void garbageLogYieldsSingleWarn() throws Exception {
        Files.writeString(tempDir.resolve("Log.txt"), "??\n");

        List<InspectionMessage> messages = inspect();

        assertThat(messages, hasSize(1));
        assertThat(messages.getFirst().getSeverity(), is(Severity.WARN));
        assertThat(messages.getFirst().getMessage(), containsString("empty or unrecognized"));
    }

    @Test
    void unreadableLogYieldsSingleError() throws Exception {
        Files.createDirectory(tempDir.resolve("Log.txt"));

        List<InspectionMessage> messages = inspect();

        assertThat(messages, hasSize(1));
        assertThat(messages.getFirst().getSeverity(), is(Severity.ERROR));
        assertThat(messages.getFirst().getMessage(), containsString("Could not read Log.txt"));
    }

    @Test
    void statsAndFindingsFromXp12Log() throws Exception {
        copyFixture("xp12-sample.txt");
        plugin("Avitab", "mac_x64/Avitab.xpl");
        plugin("FlyWithLua", "64/mac.xpl");

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> sessions = messages.stream().filter(m -> m.getSeverity() == Severity.INFO)
                .filter(m -> m.getMessage().startsWith("Session:")).toList();
        assertThat(sessions, hasSize(1));
        assertThat(sessions.getFirst().getMessage(), allOf(containsString("X-Plane 12.4.3-r2"),
                containsString("0:03:46.647"), containsString("Aircraft/Laminar Research/McDonnell Douglas MD-82")));
        assertThat(messages.stream().filter(m -> m.getSeverity() == Severity.ERROR).map(InspectionMessage::getMessage).toList(),
                hasItem(containsString("FlyWithLua")));
        assertThat(messages.stream().filter(m -> "APT".equals(m.getObject())).toList(), hasSize(1));
        InspectionMessage apt = messages.stream().filter(m -> "APT".equals(m.getObject())).findFirst().orElseThrow();
        assertThat(apt.getSeverity(), is(Severity.INFO));
        assertThat(apt.getMessage(), allOf(containsString("frequencies"), containsString("(2 occurrences)")));
        assertThat(apt.getDetails(), allOf(containsString("ETOU"), containsString("ETHB")));
        assertThat(messages.stream().noneMatch(m -> m.getMessage().contains("unclean")), is(true));
        assertThat(messages.stream().filter(m -> m.getMessage().contains("Avitab")).filter(m -> m.getSeverity() == Severity.ERROR).toList(),
                empty());
    }

    @Test
    void installedButNotLoadedPluginWarnsItMayHaveBeenAddedAfterSession() throws Exception {
        copyFixture("xp12-sample.txt");
        plugin("LiveTraffic", "mac_x64/LiveTraffic.xpl");

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> missing = messages.stream().filter(m -> m.getMessage().contains("absent from the last session")).toList();
        assertThat(missing, hasSize(1));
        InspectionMessage row = missing.getFirst();
        assertThat(row.getSeverity(), is(Severity.WARN));
        assertThat(row.getMessage(), containsString("1 installed plugin"));
        assertThat(row.getDetails(), containsString("LiveTraffic"));
    }

    @Test
    void failedPluginPathMayContainComma() throws Exception {
        String path = "/Users/test, Beta/Resources/plugins/FlyWithLua/64/mac.xpl";
        Files.writeString(tempDir.resolve("Log.txt"), "Log.txt for X-Plane 12\n"
                + "dlerror:dlopen(" + path + ", 0x0006): failed to load\n"
                + "----- X-Plane has shut down -----\n");

        List<InspectionMessage> messages = inspect();

        InspectionMessage failure = messages.stream().filter(m -> m.getSeverity() == Severity.ERROR)
                .filter(m -> m.getMessage().contains(path)).findFirst().orElseThrow();
        assertThat(failure.getDetails(), containsString("line 2: dlerror:dlopen(" + path));
    }

    @Test
    void uncleanExitWarnsWhenNoShutdownMarker() throws Exception {
        copyFixture("xp11-sample.txt");

        List<InspectionMessage> messages = inspect();

        assertThat(messages.stream().filter(m -> m.getSeverity() == Severity.WARN).map(InspectionMessage::getMessage).toList(),
                hasItem(anyOf(containsString("crashed"), containsString("still running"))));
        List<InspectionMessage> atc = messages.stream().filter(m -> "ATC".equals(m.getObject())).toList();
        assertThat(atc, hasSize(1));
        assertThat(atc.getFirst().getSeverity(), is(Severity.WARN));
        assertThat(atc.getFirst().getMessage(), containsString("(2 occurrences)"));
        assertThat(atc.getFirst().getDetails(), allOf(containsString("line 15:"), containsString("line 16:")));
    }

    @Test
    void aptErrorsAreGroupedUnderVolume() throws Exception {
        String apt = "0:00:00.000 E/APT: The airport ETOU has lost some controllers due to bad frequencies.";
        String content = "Log.txt for X-Plane 12\n" + String.join("\n", java.util.Collections.nCopies(300, apt))
                + "\n----- X-Plane has shut down -----\n";
        Files.writeString(tempDir.resolve("Log.txt"), content);

        List<InspectionMessage> aptMessages = inspect().stream().filter(m -> "APT".equals(m.getObject())).toList();

        assertThat(aptMessages, hasSize(1));
        assertThat(aptMessages.getFirst().getMessage(), containsString("(300 occurrences)"));
        List<String> lines = aptMessages.getFirst().getDetails().lines().toList();
        assertThat(lines, hasSize(300));
        assertThat(lines, everyItem(startsWith("line ")));
    }

    @Test
    void diagnosticsWithDifferentNumericPathsAreNotGrouped() throws Exception {
        Files.writeString(tempDir.resolve("Log.txt"), "Log.txt for X-Plane 12\n"
                + "0:00:01.000 W/SCN: Failed to load tile +47-125.dsf\n"
                + "0:00:02.000 W/SCN: Failed to load tile +48-125.dsf\n"
                + "----- X-Plane has shut down -----\n");

        List<InspectionMessage> sceneryMessages = inspect().stream().filter(m -> "SCN".equals(m.getObject())).toList();

        assertThat(sceneryMessages, hasSize(2));
        assertThat(sceneryMessages.stream().map(InspectionMessage::getDetails).toList(),
                allOf(hasItem(containsString("+47-125.dsf")), hasItem(containsString("+48-125.dsf"))));
    }

    @Test
    void unknownPatternsShowRepresentativeMessage() throws Exception {
        String first = "Failed to load tile +47-125.dsf because bytes are unavailable";
        String second = "Could not parse custom scenery metadata";
        Files.writeString(tempDir.resolve("Log.txt"), "Log.txt for X-Plane 12\n"
                + "0:00:01.000 W/SCN: " + first + "\n"
                + "0:00:02.000 W/SCN: " + second + "\n"
                + "----- X-Plane has shut down -----\n");

        List<InspectionMessage> scenery = inspect().stream().filter(m -> "SCN".equals(m.getObject())).toList();

        assertThat(scenery, hasSize(2));
        assertThat(scenery.getFirst().getMessage(), startsWith(first));
        assertThat(scenery.getFirst().getMessage(), not(containsString("(+")));
        assertThat(scenery.get(1).getMessage(), is(second));
        assertThat(scenery.stream().map(InspectionMessage::getDetails).toList(),
                allOf(hasItem(containsString("line 2: ")), hasItem(containsString("line 3: "))));
    }

    @Test
    void missingEnabledSceneryIsWarned() throws Exception {
        copyFixture("xp12-sample.txt");
        Path customScenery = Files.createDirectories(tempDir.resolve("Custom Scenery"));
        Files.createDirectories(customScenery.resolve("Global Airports"));
        Files.createDirectories(customScenery.resolve("Ortho Test Area"));
        Files.writeString(customScenery.resolve("scenery_packs.ini"), "I\n1000 Version\nSCENERY\n\nSCENERY_PACK Custom Scenery/Global Airports/\nSCENERY_PACK Custom Scenery/Ortho Test Area/\n");

        List<InspectionMessage> messages = inspect();

        List<InspectionMessage> sceneryMissing = messages.stream()
                .filter(m -> m.getMessage().contains("not loaded in the last session")).toList();
        assertThat(sceneryMissing, hasSize(1));
        InspectionMessage row = sceneryMissing.getFirst();
        assertThat(row.getSeverity(), is(Severity.WARN));
        assertThat(row.getMessage(), containsString("1 enabled scenery"));
        assertThat(row.getDetails(), containsString("Ortho Test Area"));
        assertThat(row.getDetails(), not(containsString("Global Airports")));
    }

    @Test
    void pairedSoundLinesCollapseIntoOneGroup() throws Exception {
        String content = "Log.txt for X-Plane 12\n"
                + "0:03:46.647 E/SOUN: snd error in file: Aircraft/MD-82/fmod/MD80.snd line 1803\n"
                + "0:03:46.647 E/SOUN: Event: event:/md80/rot_med_single is not a one-shot but is polyphonic fire-and-forget.\n"
                + "0:03:46.647 E/SOUN: snd error in file: Aircraft/MD-82/fmod/MD80.snd line 1809\n"
                + "0:03:46.647 E/SOUN: Event: event:/md80/rot_small_single is not a one-shot but is polyphonic fire-and-forget.\n"
                + "0:03:46.647 E/SOUN: snd error in file: Aircraft/MD-82/fmod/MD80.snd line 1817\n"
                + "0:03:46.647 E/SOUN: Event: event:/md80/rot_mcp_single is not a one-shot but is polyphonic fire-and-forget.\n"
                + "0:03:46.647 W/APT: Unrelated decoy warning\n"
                + "----- X-Plane has shut down -----\n";
        Files.writeString(tempDir.resolve("Log.txt"), content);

        List<InspectionMessage> messages = inspect();
        List<InspectionMessage> soundRows = messages.stream()
                .filter(m -> "SOUN".equals(m.getObject())).toList();

        assertThat(soundRows, hasSize(1));
        InspectionMessage row = soundRows.getFirst();
        assertThat(row.getSeverity(), is(Severity.WARN));
        assertThat(row.getMessage(), allOf(containsString("polyphonic"), containsString("(3 occurrences)")));

        List<String> details = row.getDetails().lines().toList();
        assertThat(details, hasSize(6));
        assertThat(details, everyItem(startsWith("line ")));
        for (int i = 0; i < details.size(); i += 2) {
            assertThat(details.get(i), containsString("snd error in file"));
            assertThat(details.get(i + 1), containsString("polyphonic fire-and-forget"));
        }

        List<InspectionMessage> aptRows = messages.stream().filter(m -> "APT".equals(m.getObject())).toList();
        assertThat(aptRows, hasSize(1));
    }

    @Test
    void browserLanguageNoiseIsDroppedFromOutput() throws Exception {
        Files.writeString(tempDir.resolve("Log.txt"), "Log.txt for X-Plane 12\n"
                + "0:00:11.334 W/LOGIN: Setting browser language to allow: en\n"
                + "0:01:24.460 W/LOGIN: Setting browser language to allow: en\n"
                + "0:00:00.000 E/APT: The airport ETOU has lost some controllers due to bad frequencies.\n"
                + "----- X-Plane has shut down -----\n");

        List<InspectionMessage> messages = inspect();

        assertThat(messages.stream().filter(m -> m.getMessage().contains("browser language")).toList(), empty());
        assertThat(messages.stream().filter(m -> "APT".equals(m.getObject())).toList(), hasSize(1));
    }

    @Test
    void sndErrorWithoutFollowingLineIsStillReported() throws Exception {
        Files.writeString(tempDir.resolve("Log.txt"), "Log.txt for X-Plane 12\n"
                + "0:03:46.647 E/SOUN: snd error in file: Aircraft/MD-82/fmod/MD80.snd line 1999\n"
                + "----- X-Plane has shut down -----\n");

        List<InspectionMessage> soundRows = inspect().stream()
                .filter(m -> "SOUN".equals(m.getObject())).toList();

        assertThat(soundRows, hasSize(1));
        InspectionMessage row = soundRows.getFirst();
        assertThat(row.getSeverity(), is(Severity.WARN));
        assertThat(row.getDetails().lines().toList(), hasSize(1));
        assertThat(row.getDetails(), containsString("snd error in file"));
    }

    private List<InspectionMessage> inspect() throws Exception {
        return new StartLogInspection().inspect(new XPlane(tempDir)).getMessages();
    }

    private void copyFixture(String fixture) throws IOException {
        try (var input = getClass().getResourceAsStream("/log/" + fixture)) {
            String text = new String(input.readAllBytes()).replace("/Users/testuser/Applications/X-Plane 12", tempDir.toString());
            Files.writeString(tempDir.resolve("Log.txt"), text);
        }
    }

    private void plugin(String folder, String executable) throws IOException {
        Path path = Files.createDirectories(tempDir.resolve("Resources/plugins").resolve(folder)
                .resolve(executable).getParent()).resolve(Path.of(executable).getFileName());
        Files.writeString(path, "");
    }
}
