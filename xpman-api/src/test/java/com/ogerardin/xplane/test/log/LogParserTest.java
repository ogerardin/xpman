package com.ogerardin.xplane.test.log;

import com.ogerardin.xplane.log.LogEntry;
import com.ogerardin.xplane.log.LogParser;
import com.ogerardin.xplane.log.SessionLog;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LogParserTest {

    @Test
    void parsesXp12HeaderAndFacts() throws IOException {
        SessionLog log = parse("xp12-sample.txt");

        assertThat(log.getVersionLine(), is("X-Plane 12.4.3-r2-15ff1e4d (build 124311 Apple Silicon, Metal d50c1d1e582274be2ba7099464743271b8900da8)"));
        assertThat(log.getStartedAt(), is(LocalDateTime.of(2026, 10, 23, 23, 39, 14)));
        assertThat(log.hasCleanExit(), is(true));
        assertThat(log.loadedPluginPaths(), is(List.of("/Users/testuser/Applications/X-Plane 12/Resources/plugins/Avitab/mac_x64/Avitab.xpl")));
        assertThat(log.failedPluginPaths(), is(List.of("/Users/testuser/Applications/X-Plane 12/Resources/plugins/FlyWithLua/64/mac.xpl")));
        assertThat(log.sceneryPacks(), is(List.of("Custom Scenery/Global Airports", "Custom Scenery/KSEA Demo Area", "Global Scenery/X-Plane 12 Global Scenery")));
        assertThat(log.menuShownAt(), is(Optional.of(Duration.ofMillis(8077))));
        assertThat(log.flightStartedAt(), is(Optional.of(Duration.parse("PT3M46.647S"))));
        assertThat(log.aircraftPath(), is(Optional.of("Aircraft/Laminar Research/McDonnell Douglas MD-82/MD80.acf")));
        assertThat(log.lastElapsed(), is(Optional.of(Duration.parse("PT12M26.675S"))));
        assertThat(log.dsfLoads(), hasSize(2));
        assertThat(log.dsfLoads().getFirst(), is(new SessionLog.DsfLoad("Custom Scenery/Global Airports/Earth nav data/+40-130/+47-125.dsf", 757358L)));
    }

    @Test
    void parsesTaggedLineWithMultiLevelSubsystem() throws IOException {
        LogEntry entry = parse("xp12-sample.txt").getEntries().stream().filter(e -> e.level() != null).findFirst().orElseThrow();

        assertThat(entry.level(), is(LogEntry.Level.INFO));
        assertThat(entry.subsystem(), is("GFX/MTL"));
        assertThat(entry.elapsed(), is(Duration.ZERO));
    }

    @Test
    void parsesXp11LowercaseHeaderAndUncleanExit() throws IOException {
        SessionLog log = parse("xp11-sample.txt");

        assertThat(log.getVersionLine(), is("X-Plane 11.32r2 (build 113201 64-bit)"));
        assertThat(log.getStartedAt(), is(LocalDateTime.of(2019, 4, 19, 21, 43, 2)));
        assertThat(log.hasCleanExit(), is(false));
        assertThat(log.menuShownAt(), is(Optional.of(Duration.ofMillis(12731))));
    }

    @Test
    void parsesLegacyUntaggedLog() throws IOException {
        SessionLog log = parse("legacy-untagged.txt");

        assertThat(log.getVersionLine(), startsWith("X-Plane 10.50"));
        assertThat(log.hasCleanExit(), is(true));
        assertThat(log.loadedPluginPaths(), hasSize(1));
        assertThat(log.getEntries().stream().allMatch(entry -> entry.level() == null), is(true));
        assertThat(log.getEntries().stream().allMatch(entry -> entry.subsystem() == null), is(true));
        assertThat(log.menuShownAt(), is(Optional.empty()));
    }

    @Test
    void parsesTruncatedFinalLine() throws IOException {
        SessionLog log = parse("xp11-sample.txt");

        assertThat(log.getEntries().getLast().raw(), is("0:00:28.100 I/SCN: DSF load time: 42"));
        assertThat(log.lastElapsed(), is(Optional.of(Duration.parse("PT28.100S"))));
    }

    @Test
    void parsesManyIdenticalLines() {
        String line = "0:00:00.000 E/APT: The airport ETOU has lost some controllers due to bad frequencies.";
        SessionLog log = LogParser.parse(java.util.stream.Stream.concat(java.util.stream.Stream.of("Log.txt for X-Plane 12"),
                java.util.stream.IntStream.range(0, 500).mapToObj(_ -> line)).toList());

        assertThat(log.getEntries(), hasSize(501));
    }

    @Test
    void blankLinesAndGarbageAreKeptAsRawEntries() {
        SessionLog log = LogParser.parse(List.of("", "   ", "??"));

        assertThat(log.getEntries(), hasSize(3));
        assertThat(log.getEntries().stream().allMatch(entry -> entry.level() == null), is(true));
        assertThat(log.getEntries().stream().allMatch(entry -> entry.subsystem() == null), is(true));
        assertThat(log.getVersionLine(), nullValue());
    }

    private SessionLog parse(String fixture) throws IOException {
        try (var input = getClass().getResourceAsStream("/log/" + fixture)) {
            Path file = Files.createTempFile("xpman-log-", ".txt");
            Files.copy(input, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            try {
                return LogParser.parse(file);
            } finally {
                Files.deleteIfExists(file);
            }
        }
    }
}
