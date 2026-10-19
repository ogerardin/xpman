package com.ogerardin.xplane.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Numeric comparison of X-Plane version strings.
 * <p>
 * Handles the legacy "12.04r3" = 12.0.4 layout (X-Plane 12 early minor releases),
 * modern "12.4.3" / "12.4.3-r2-..." layout, and ignores non-numeric suffixes such
 * as "-b4-fbbdaacd" (beta/hash).
 */
@UtilityClass
public class Versions {

    /**
     * Old-style X-Plane 12 version pattern: "12.04r3" = 12.0.4.
     * Matches the house convention in HomeController.compareVersions / normalizeVersion.
     */
    private static final Pattern OLD_STYLE = Pattern.compile("(\\d+)\\.0(\\d)(.*)$");

    /**
     * Compares two X-Plane version strings numerically component by component.
     * Missing trailing components are treated as 0 (so "12.4" equals "12.4.0").
     *
     * @return negative / zero / positive as v1 is less than / equal to / greater than v2
     */
    public int compare(String v1, String v2) {
        String[] parts1 = normalize(v1).split("\\.");
        String[] parts2 = normalize(v2).split("\\.");
        int maxLen = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < maxLen; i++) {
            int c1 = i < parts1.length ? parseLeadingDigits(parts1[i]) : 0;
            int c2 = i < parts2.length ? parseLeadingDigits(parts2[i]) : 0;
            if (c1 != c2) return Integer.compare(c1, c2);
        }
        return 0;
    }

    /**
     * Re-applies the legacy "12.04r3" → "12.0.4r3" rewrite (house convention),
     * leaving modern versions unchanged.
     */
    private String normalize(String v) {
        if (v == null) return "";
        Matcher m = OLD_STYLE.matcher(v);
        if (m.matches()) {
            // "12.04r3" → "12.0.4r3"
            return m.group(1) + ".0." + m.group(2) + m.group(3);
        }
        return v;
    }

    private int parseLeadingDigits(String s) {
        int value = 0;
        int i = 0;
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            value = value * 10 + (s.charAt(i) - '0');
            i++;
        }
        return value;
    }
}
