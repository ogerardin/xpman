package com.ogerardin.xplane.inspection.impl;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xplane.inspection.Severity;
import com.ogerardin.xplane.log.LogEntry;
import com.ogerardin.xplane.log.SessionLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects crash-related diagnostics from X-Plane log entries.
 */
class CrashLogDiagnostics {

    private static final Pattern CRASH_PATTERN = Pattern.compile(
            "(?i)(This application has crashed|crashed because of the plugin|Fatal signal:|terminated with signal)"
    );

    private static final Pattern BACKTRACE_PATTERN = Pattern.compile(
            "(?i)(Backtrace:|Stack dump:)"
    );

    private static final Pattern FRAME_PATTERN = Pattern.compile(
            "(?i)frame\\s+#\\d+.*|0x[0-9a-fA-F]+\\s+\\S+"
    );

    // Stricter pattern for backtrace frames: must have "frame #N:" or "0xADDR in" context
    private static final Pattern STRICT_FRAME_PATTERN = Pattern.compile(
            "(?i)(?:frame\\s+#\\d+\\s*:.*|0x[0-9a-fA-F]+\\s+in\\s+\\S+)"
    );

    private static final Pattern SIGNAL_PATTERN = Pattern.compile(
            "(?i)SIGSEGV|SIGBUS|SIGABRT|EXC_BAD_ACCESS"
    );

    static List<InspectionMessage> collect(SessionLog log, Set<Integer> consumedLines) {
        List<InspectionMessage> findings = new ArrayList<>();
        Set<Integer> processedLineIndices = new HashSet<>();

        // Find crash indicators with their indices
        List<LogEntry> crashLines = new ArrayList<>();
        List<Integer> crashLineNumbers = new ArrayList<>();
        List<LogEntry> backtraceLines = new ArrayList<>();
        List<Integer> backtraceLineNumbers = new ArrayList<>();
        List<String> signals = new ArrayList<>();

        for (int i = 0; i < log.getEntries().size(); i++) {
            LogEntry entry = log.getEntries().get(i);
            int lineNumber = i + 1; // 1-based line number
            String raw = entry.raw();

            // Check for crash indicators
            if (CRASH_PATTERN.matcher(raw).find()) {
                crashLines.add(entry);
                crashLineNumbers.add(lineNumber);
                processedLineIndices.add(lineNumber);
            }

            // Check for backtrace headers
            if (BACKTRACE_PATTERN.matcher(raw).find()) {
                backtraceLines.add(entry);
                backtraceLineNumbers.add(lineNumber);
                processedLineIndices.add(lineNumber);

                // Collect frames after backtrace header
                for (int j = i + 1; j < log.getEntries().size(); j++) {
                    LogEntry frameEntry = log.getEntries().get(j);
                    if (STRICT_FRAME_PATTERN.matcher(frameEntry.raw()).find()) {
                        backtraceLines.add(frameEntry);
                        backtraceLineNumbers.add(j + 1);
                        processedLineIndices.add(j + 1);
                    } else {
                        break; // Stop at first non-frame line
                    }
                }
            }

            // Also collect frames that appear directly after crash lines (without header)
            if (CRASH_PATTERN.matcher(raw).find() && !crashLines.isEmpty() && crashLines.getLast() == entry) {
                // Look ahead for frames
                for (int j = i + 1; j < log.getEntries().size(); j++) {
                    LogEntry frameEntry = log.getEntries().get(j);
                    String frameRaw = frameEntry.raw();
                    // Stop if we hit another crash indicator or backtrace header
                    if (CRASH_PATTERN.matcher(frameRaw).find() || BACKTRACE_PATTERN.matcher(frameRaw).find()) {
                        break;
                    }
                    if (STRICT_FRAME_PATTERN.matcher(frameRaw).find() && !backtraceLineNumbers.contains(j + 1)) {
                        backtraceLines.add(frameEntry);
                        backtraceLineNumbers.add(j + 1);
                        processedLineIndices.add(j + 1);
                    } else if (!STRICT_FRAME_PATTERN.matcher(frameRaw).find()) {
                        break; // Stop at first non-frame line
                    }
                }
            }

            // Check for signals
            if (SIGNAL_PATTERN.matcher(raw).find()) {
                signals.addAll(extractSignals(raw));
            }
        }

        // Create crash findings
        // Only create a crash finding if we have explicit crash evidence
        if (!crashLines.isEmpty()) {
            findings.add(createCrashFinding(crashLines, crashLineNumbers, backtraceLines, backtraceLineNumbers,
                    signals, processedLineIndices, consumedLines));
        }

        return findings;
    }

    private static List<String> extractSignals(String text) {
        List<String> signals = new ArrayList<>();
        java.util.regex.Matcher matcher = SIGNAL_PATTERN.matcher(text);
        while (matcher.find()) {
            signals.add(matcher.group().toUpperCase());
        }
        return signals;
    }

    private static InspectionMessage createCrashFinding(List<LogEntry> crashLines, List<Integer> crashLineNumbers,
                                                         List<LogEntry> backtraceLines, List<Integer> backtraceLineNumbers,
                                                         List<String> signals, Set<Integer> processedLineIndices,
                                                         Set<Integer> consumedLines) {
        StringBuilder message = new StringBuilder();
        StringBuilder details = new StringBuilder();
        StringBuilder searchQuery = new StringBuilder("X-Plane crash");

        // Extract plugin name if available
        String pluginName = extractPluginName(crashLines);

        // Build message
        if (!crashLines.isEmpty()) {
            String firstCrash = crashLines.get(0).raw();
            if (firstCrash.contains("because of the plugin") && pluginName != null) {
                message.append("X-Plane crashed because of the plugin: ").append(pluginName);
            } else {
                message.append("X-Plane crashed");
            }
        }

        // Add signal information
        if (!signals.isEmpty()) {
            String uniqueSignals = String.join(", ", new HashSet<>(signals));
            if (!message.isEmpty()) {
                message.append(" with ");
            } else {
                message.append("X-Plane terminated");
            }
            message.append("fatal signal: ").append(uniqueSignals);
            searchQuery.append(" ").append(uniqueSignals);
        }

        // Combine and sort all lines by line number
        record EntryWithLine(LogEntry entry, int lineNumber) {}
        List<EntryWithLine> allLines = new ArrayList<>();

        for (int i = 0; i < crashLines.size(); i++) {
            allLines.add(new EntryWithLine(crashLines.get(i), crashLineNumbers.get(i)));
        }
        for (int i = 0; i < backtraceLines.size(); i++) {
            allLines.add(new EntryWithLine(backtraceLines.get(i), backtraceLineNumbers.get(i)));
        }

        // Sort by line number
        allLines.sort((a, b) -> Integer.compare(a.lineNumber(), b.lineNumber()));

        // Mark ALL crash evidence lines as consumed (before filtering for details)
        for (EntryWithLine entryWithLine : allLines) {
            consumedLines.add(entryWithLine.lineNumber());
        }

        // Build details - include signal info and frames (exclude Backtrace: header and crash message)
        for (EntryWithLine entryWithLine : allLines) {
            String raw = entryWithLine.entry().raw();
            // Skip the "Backtrace:" or "Stack dump:" header
            if (BACKTRACE_PATTERN.matcher(raw).find()) {
                continue;
            }
            // Skip the crash message itself (already in message field)
            // But keep signal lines (they're not crash messages, they're evidence)
            if (CRASH_PATTERN.matcher(raw).find() && !SIGNAL_PATTERN.matcher(raw).find()) {
                continue;
            }
            details.append("line ").append(entryWithLine.lineNumber()).append(": ")
                    .append(raw).append("\n");
        }

        if (details.length() > 0) {
            details.setLength(details.length() - 1); // Remove trailing newline
        }

        return InspectionMessage.builder()
                .severity(Severity.ERROR)
                .object("Crash")
                .message(message.toString())
                .details(details.toString())
                .searchQuery(searchQuery.toString())
                .build();
    }

    private static String extractPluginName(List<LogEntry> crashLines) {
        Pattern pluginPattern = Pattern.compile("because of the plugin:\\s*(.+)");
        for (LogEntry entry : crashLines) {
            java.util.regex.Matcher matcher = pluginPattern.matcher(entry.raw());
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }
        return null;
    }
}
