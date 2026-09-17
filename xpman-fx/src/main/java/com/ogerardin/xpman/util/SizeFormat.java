package com.ogerardin.xpman.util;

import lombok.experimental.UtilityClass;

/**
 * Formatting helpers for human-readable values.
 */
@UtilityClass
public class SizeFormat {

    /** Formats a byte count as a human-readable string (e.g. "1.5 MB"). */
    public static String humanSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format("%.1f MB", bytes / 1024.0 / 1024);
        }
        return String.format("%.1f GB", bytes / 1024.0 / 1024 / 1024);
    }
}