package com.ogerardin.xplane.log;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses XP11 and XP12 timestamped and legacy untagged log lines. */
@UtilityClass
public class LogParser {

    private static final Pattern TAGGED = Pattern.compile("^(\\d+):(\\d{2}):(\\d{2})\\.(\\d{3}) ([IWED])/([\\w/]+): (.*)$");
    private static final Pattern VERSION = Pattern.compile("^log\\.txt for (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMPILED = Pattern.compile("^compiled on (.+)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern STARTED = Pattern.compile("^X-Plane started on (.+)$", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter START_DATE = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy", Locale.ENGLISH);

    public SessionLog parse(Path file) throws IOException {
        return parse(Files.readAllLines(file));
    }

    public SessionLog parse(List<String> lines) {
        String version = null;
        String compiled = null;
        LocalDateTime started = null;
        var entries = new ArrayList<LogEntry>(lines.size());
        for (String line : lines) {
            Matcher matcher = VERSION.matcher(line);
            if (matcher.matches()) version = matcher.group(1);
            matcher = COMPILED.matcher(line);
            if (matcher.matches()) compiled = matcher.group(1);
            matcher = STARTED.matcher(line);
            if (matcher.matches()) {
                try {
                    started = LocalDateTime.parse(matcher.group(1), START_DATE);
                } catch (DateTimeParseException ignored) {
                    // Keep parsing; malformed header dates do not invalidate the rest of the log.
                }
            }
            matcher = TAGGED.matcher(line);
            if (matcher.matches()) {
                long seconds = Long.parseLong(matcher.group(1)) * 3600
                        + Long.parseLong(matcher.group(2)) * 60 + Long.parseLong(matcher.group(3));
                Duration elapsed = Duration.ofSeconds(seconds).plusMillis(Long.parseLong(matcher.group(4)));
                entries.add(new LogEntry(elapsed, LogEntry.Level.of(matcher.group(5).charAt(0)),
                        matcher.group(6), matcher.group(7), line));
            } else {
                entries.add(new LogEntry(null, null, null, line, line));
            }
        }
        return new SessionLog(version, compiled, started, entries);
    }
}
