package com.ogerardin.xplane.log;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parsed header, lines, and derived facts from a single X-Plane log. */
@Getter
public class SessionLog {

    private static final Pattern LOADED = Pattern.compile("^Loaded: (.+) \\([^)]+\\)\\.$");
    private static final Pattern FAILED = Pattern.compile("^dlerror:dlopen\\((.+), 0x[0-9a-fA-F]+\\):.*$");
    private static final Pattern FLIGHT = Pattern.compile("^Starting new flight in (.+) at .+$");
    private static final Pattern DSF = Pattern.compile("^DSF load time: (\\d+) for file (.+?) \\(");

    private final String versionLine;
    private final String compiledLine;
    private final LocalDateTime startedAt;
    private final List<LogEntry> entries;

    public SessionLog(String versionLine, String compiledLine, LocalDateTime startedAt, List<LogEntry> entries) {
        this.versionLine = versionLine;
        this.compiledLine = compiledLine;
        this.startedAt = startedAt;
        this.entries = List.copyOf(entries);
    }

    public boolean hasCleanExit() {
        return entries.stream().anyMatch(entry -> entry.raw().equals("----- X-Plane has shut down -----"));
    }

    public List<String> loadedPluginPaths() {
        return matchingPaths(LOADED);
    }

    public List<String> failedPluginPaths() {
        return matchingPaths(FAILED);
    }

    private List<String> matchingPaths(Pattern pattern) {
        return entries.stream().map(LogEntry::raw).map(pattern::matcher)
                .filter(Matcher::matches).map(matcher -> matcher.group(1)).toList();
    }

    public List<String> sceneryPacks() {
        var packs = new java.util.ArrayList<String>();
        boolean inList = false;
        for (LogEntry entry : entries) {
            String line = entry.raw();
            if (line.startsWith("I found the following scenery packages")) {
                inList = true;
                continue;
            }
            if (!inList) continue;
            Matcher item = Pattern.compile("^\\d+ (.+)$").matcher(line);
            if (!item.matches()) break;
            packs.add(item.group(1).replaceAll("/+$", ""));
        }
        return List.copyOf(packs);
    }

    public Optional<Duration> menuShownAt() {
        return entries.stream().filter(entry -> "WIN".equals(entry.subsystem()))
                .filter(entry -> entry.message().startsWith("Showing subscreen Main Menu"))
                .map(LogEntry::elapsed).filter(java.util.Objects::nonNull).findFirst();
    }

    public Optional<Duration> flightStartedAt() {
        return entries.stream().filter(entry -> "FCG".equals(entry.subsystem()))
                .filter(entry -> FLIGHT.matcher(entry.message()).matches())
                .map(LogEntry::elapsed).filter(java.util.Objects::nonNull).findFirst();
    }

    public Optional<String> aircraftPath() {
        return entries.stream().filter(entry -> "FCG".equals(entry.subsystem()))
                .map(LogEntry::message).map(FLIGHT::matcher).filter(Matcher::matches)
                .map(matcher -> matcher.group(1)).findFirst();
    }

    public Optional<Duration> lastElapsed() {
        return entries.stream().map(LogEntry::elapsed).filter(java.util.Objects::nonNull).max(Duration::compareTo);
    }

    public List<DsfLoad> dsfLoads() {
        return entries.stream().filter(entry -> "SCN".equals(entry.subsystem()))
                .map(LogEntry::message).map(DSF::matcher).filter(Matcher::find)
                .map(matcher -> new DsfLoad(matcher.group(2), Long.parseLong(matcher.group(1)))).toList();
    }

    public record DsfLoad(String file, long micros) { }
}
