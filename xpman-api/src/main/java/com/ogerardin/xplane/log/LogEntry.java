package com.ogerardin.xplane.log;

import java.time.Duration;

/** One line of an X-Plane log, with tagged metadata when available. */
public record LogEntry(Duration elapsed, Level level, String subsystem, String message, String raw) {

    public enum Level {
        DEBUG, INFO, WARN, ERROR;

        public static Level of(char value) {
            return switch (value) {
                case 'D' -> DEBUG;
                case 'I' -> INFO;
                case 'W' -> WARN;
                case 'E' -> ERROR;
                default -> INFO;
            };
        }
    }
}
