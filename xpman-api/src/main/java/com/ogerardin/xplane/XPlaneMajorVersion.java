package com.ogerardin.xplane;

import com.ogerardin.xplane.laminar.UpdateInformation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Represents a major version of X-Plane such as 11, 12 and the associated specifics.
 */
@AllArgsConstructor
@Getter
public enum XPlaneMajorVersion {
    XP11(11,
            version -> version.startsWith("11"),
            "http://lookup-a.x-plane.com/_lookup_11_/server_list_11.txt",
            version -> String.format("https://www.x-plane.com/kb/x-plane-11-%s-release-notes", minor(version))
    ),

    XP12(12,
            version -> version.startsWith("12"),
            "https://lookup.x-plane.com/_lookup_12_/server_list_12.txt",
            version -> String.format("https://www.x-plane.com/kb/x-plane-%s-release-notes/#%s",
                    version.replace(".", "-"),
                    version.replace(".", ""))
    );


    public static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)(.*)");

    private static String minor(String version) {
        return VERSION_PATTERN.matcher(version).group(2);
    }

    /** The major integer version */
    private final int major;
    /** A predicate that recognizes that a version string matches this major */
    private final Predicate<String> matcher;
    /** The URL of the server list file for this major version */
    private final String serverListUrl;
    /** A function that takes a full version string and produces the URL for the release notes of this version */
    private final Function<String, String> releaseNotesUrlBuilder;

    public static XPlaneMajorVersion of(String version) {
        return Arrays.stream(values())
                .filter(v -> v.matcher.test(version))
                .findFirst()
                .orElseThrow();
    }

    /** Returns the 4-digit spec string for this version, e.g. "1100" for X-Plane 11. */
    public String specString() {
        return String.format("%02d00", major);
    }

    public UpdateInformation getUpdateInformation() {
        return new UpdateInformation(this);
    }
}
