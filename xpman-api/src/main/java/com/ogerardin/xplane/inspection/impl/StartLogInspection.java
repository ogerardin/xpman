package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.inspection.Inspection;
import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.log.LogEntry;
import com.ogerardin.xplane.log.LogParser;
import com.ogerardin.xplane.log.KnownLogIssue;
import com.ogerardin.xplane.log.SessionLog;
import com.ogerardin.xplane.scenery.SceneryEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Inspects the last X-Plane session log for diagnostics and session statistics. */
@Slf4j
public class StartLogInspection implements Inspection<XPlane> {

    private static final Pattern SND_ERROR_PATTERN = Pattern.compile("snd error in file: .* line \\d+");

    @Override
    public InspectionResult inspect(XPlane xPlane) {
        Path logPath = xPlane.getLogPath();
        if (!Files.exists(logPath)) {
            return InspectionResult.of(InspectionMessage.error("Log.txt not found: " + logPath));
        }

        SessionLog log;
        try {
            log = LogParser.parse(logPath);
        } catch (IOException | SecurityException e) {
            return InspectionResult.of(InspectionMessage.builder().severity(Severity.ERROR)
                    .message("Could not read Log.txt: " + e.getMessage()).build());
        }
        boolean recognized = log.getVersionLine() != null || log.getCompiledLine() != null || log.getStartedAt() != null
                || log.getEntries().stream().anyMatch(entry -> entry.level() != null
                || entry.raw().startsWith("Loaded:") || entry.raw().startsWith("dlerror:dlopen(")
                || entry.raw().equals("----- X-Plane has shut down -----")
                || entry.raw().startsWith("I found the following scenery packages"));
        if (!recognized) {
            return InspectionResult.of(InspectionMessage.builder().severity(Severity.WARN)
                    .message("Log.txt is empty or unrecognized").build());
        }

        var messages = new ArrayList<InspectionMessage>();
        addStatistics(log, messages);
        addFailedPlugins(log, messages);
        addUnloadedPlugins(xPlane, log, messages);
        addTaggedDiagnostics(log, messages);
        addUnloadedScenery(xPlane, log, messages);
        if (!log.hasCleanExit()) {
            messages.add(InspectionMessage.builder().severity(Severity.WARN)
                    .message("No clean shutdown marker — X-Plane crashed, is still running, or the session hung").build());
        }
        return InspectionResult.of(messages);
    }

    private static void addStatistics(SessionLog log, List<InspectionMessage> messages) {
        StringBuilder session = new StringBuilder("Session: ")
                .append(log.getVersionLine() == null ? "unknown X-Plane version" : log.getVersionLine());
        if (log.getStartedAt() != null) session.append(" — started ").append(log.getStartedAt());
        log.lastElapsed().ifPresent(time -> session.append(", covered ").append(formatDuration(time)));
        session.append(", ").append(log.hasCleanExit() ? "clean" : "unclean").append(" exit");
        log.aircraftPath().ifPresent(path -> session.append(", flight ").append(path)
                .append(" at ").append(log.flightStartedAt().map(StartLogInspection::formatDuration).orElse("unknown time")));
        log.menuShownAt().ifPresent(time -> session.append(", main menu at ").append(formatDuration(time)));
        session.append(", plugins loaded/failed ").append(log.loadedPluginPaths().size()).append('/')
                .append(log.failedPluginPaths().size());
        List<SessionLog.DsfLoad> dsfLoads = log.dsfLoads();
        session.append(", scenery packs ").append(log.sceneryPacks().size());
        if (!dsfLoads.isEmpty()) session.append(String.format(Locale.ROOT, ", DSF load %.3f s", dsfLoads.stream()
                .mapToLong(SessionLog.DsfLoad::micros).sum() / 1_000_000.0));
        messages.add(InspectionMessage.info(session.toString()));
    }

    private static void addFailedPlugins(SessionLog log, List<InspectionMessage> messages) {
        for (String path : log.failedPluginPaths().stream().distinct().toList()) {
            Path pluginFolder = Path.of(path).getParent();
            if (pluginFolder != null) pluginFolder = pluginFolder.getParent();
            String folder = pluginFolder != null && pluginFolder.getFileName() != null
                    ? pluginFolder.getFileName().toString() : path;
            List<Integer> sourceLines = java.util.stream.IntStream.range(0, log.getEntries().size())
                    .filter(i -> log.getEntries().get(i).raw().startsWith("dlerror:dlopen(" + path + ","))
                    .map(i -> i + 1).boxed().toList();
            String details = sourceLines.stream()
                    .map(line -> "line " + line + ": " + log.getEntries().get(line - 1).raw())
                    .collect(java.util.stream.Collectors.joining("\n"));
            messages.add(InspectionMessage.builder().severity(Severity.ERROR).object(folder)
                    .message("Plugin failed to load: " + path).details(details).build());
        }
    }

    private static void addUnloadedPlugins(XPlane xPlane, SessionLog log, List<InspectionMessage> messages) {
        List<Path> loaded = log.loadedPluginPaths().stream().map(Path::of).map(Path::normalize).toList();
        List<Path> failed = log.failedPluginPaths().stream().map(Path::of).map(Path::normalize).toList();
        List<com.ogerardin.xplane.plugins.Plugin> missing = xPlane.getPluginManager().getPlugins().stream()
                .filter(plugin -> plugin.isEnabled())
                .filter(plugin -> !loaded.contains(plugin.getXplFile().normalize()))
                .filter(plugin -> !failed.contains(plugin.getXplFile().normalize()))
                .toList();
        if (missing.isEmpty()) return;
        String message = missing.size() + " installed plugin" + (missing.size() == 1 ? " is" : "s are")
                + " absent from the last session's loaded list; they may have been installed after this session";
        String details = missing.stream()
                .map(plugin -> plugin.getName() + " (" + plugin.getXplFile() + ")")
                .collect(java.util.stream.Collectors.joining("\n"));
        messages.add(InspectionMessage.builder().severity(Severity.WARN)
                .message(message).details(details).build());
    }

    private static void addTaggedDiagnostics(SessionLog log, List<InspectionMessage> messages) {
        Map<String, List<SourceEntry>> grouped = new LinkedHashMap<>();
        Map<String, KnownLogIssue> knownIssues = new LinkedHashMap<>();
        Set<String> pairedKeys = new java.util.HashSet<>();
        for (int i = 0; i < log.getEntries().size(); i++) {
            LogEntry entry = log.getEntries().get(i);
            if (entry.level() != LogEntry.Level.ERROR && entry.level() != LogEntry.Level.WARN) continue;
            if (KnownLogIssue.isNoise(entry.message())) continue;

            // Pair snd-error + following SOUN line if adjacent
            if (SND_ERROR_PATTERN.matcher(entry.message()).matches() && i + 1 < log.getEntries().size()) {
                LogEntry next = log.getEntries().get(i + 1);
                if (next.level() == LogEntry.Level.ERROR && "SOUN".equals(next.subsystem())) {
                    KnownLogIssue knownIssue = KnownLogIssue.matching(next.message()).orElse(null);
                    String key = knownIssue == null ? "PAIR|" + entry.message() + "|" + next.message() : knownIssue.name();
                    grouped.computeIfAbsent(key, _ -> new ArrayList<>())
                            .add(new SourceEntry(i + 1, entry));
                    grouped.get(key).add(new SourceEntry(i + 2, next));
                    if (knownIssue != null) knownIssues.put(key, knownIssue);
                    pairedKeys.add(key);
                    i++; // skip next entry
                    continue;
                }
            }

            // Regular single-line diagnostic
            KnownLogIssue knownIssue = KnownLogIssue.matching(entry.message()).orElse(null);
            String key = knownIssue == null ? entry.level() + "|" + entry.subsystem() + "|" + entry.message()
                    : knownIssue.name();
            grouped.computeIfAbsent(key, _ -> new ArrayList<>()).add(new SourceEntry(i + 1, entry));
            if (knownIssue != null) knownIssues.put(key, knownIssue);
        }
        grouped.entrySet().stream()
                .sorted(Comparator.comparingInt(item -> severityOrder(item.getKey(), item.getValue(), knownIssues)))
                .forEach(item -> {
                    List<SourceEntry> lines = item.getValue();
                    KnownLogIssue knownIssue = knownIssues.get(item.getKey());
                    LogEntry first = lines.getFirst().entry();
                    // Count occurrences: for paired lines, count pairs not individual lines
                    int occurrences = pairedKeys.contains(item.getKey()) ? lines.size() / 2 : lines.size();
                    String message = knownIssue == null ? truncate(first.message())
                            + (occurrences > 1 ? " (+" + (occurrences - 1) + " more)" : "")
                            : knownIssue.getExplanation() + " (" + occurrences + " occurrences)";
                    String details = lines.stream()
                            .map(line -> "line " + line.number() + ": " + line.entry().raw())
                            .collect(java.util.stream.Collectors.joining("\n"));
                    messages.add(InspectionMessage.builder()
                            .severity(knownIssue == null ? severity(first.level()) : knownIssue.getSeverity())
                            .object(first.subsystem()).message(message).details(details).build());
                });
    }

    private static int severityOrder(String key, List<SourceEntry> entries, Map<String, KnownLogIssue> knownIssues) {
        KnownLogIssue issue = knownIssues.get(key);
        return switch (issue == null ? severity(entries.getFirst().entry().level()) : issue.getSeverity()) {
            case ERROR -> 0;
            case WARN -> 1;
            case INFO -> 2;
        };
    }

    private static Severity severity(LogEntry.Level level) {
        return level == LogEntry.Level.ERROR ? Severity.ERROR : Severity.WARN;
    }

    private static String truncate(String message) {
        return message.length() <= 120 ? message : message.substring(0, 117) + "...";
    }

    private record SourceEntry(int number, LogEntry entry) { }

    private static void addUnloadedScenery(XPlane xPlane, SessionLog log, List<InspectionMessage> messages) {
        List<String> loaded = log.sceneryPacks().stream().map(StartLogInspection::normalizePack).toList();
        List<String> missing = new ArrayList<>();
        for (SceneryEntry entry : xPlane.getSceneryManager().getSceneryEntries()) {
            if (entry.getIniItem() == null || entry.getIniItem().isDisabled() || entry.isToken()) continue;
            String iniValue = entry.getIniItem().getIniValue();
            if (!loaded.contains(normalizePack(iniValue))) {
                missing.add(iniValue);
            }
        }
        if (missing.isEmpty()) return;
        String message = missing.size() + " enabled scenery pack" + (missing.size() == 1 ? " was" : "s were")
                + " not loaded in the last session; they may have been added after this session";
        String details = String.join("\n", missing);
        messages.add(InspectionMessage.builder().severity(Severity.WARN)
                .message(message).details(details).build());
    }

    private static String normalizePack(String value) {
        return value.replaceAll("[\\\\/]+$", "");
    }

    private static String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        return String.format(Locale.ROOT, "%d:%02d:%02d.%03d", millis / 3_600_000,
                millis / 60_000 % 60, millis / 1_000 % 60, millis % 1_000);
    }
}
