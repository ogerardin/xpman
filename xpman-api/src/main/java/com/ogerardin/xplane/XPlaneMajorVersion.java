package com.ogerardin.xplane;

import com.ogerardin.xplane.laminar.UpdateInformation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
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
            XPlaneMajorVersion::xp11ReleaseNotes
    ),

    XP12(12,
            version -> version.startsWith("12"),
            "https://lookup.x-plane.com/_lookup_12_/server_list_12.txt",
            XPlaneMajorVersion::xp12ReleaseNotes
    );

    public static final Pattern LEGACY_VERSION_PATTERN  = Pattern.compile("^(\\d+)\\.(\\d+)(.*)");
    public static final Pattern NEW_VERSION_PATTERN  = Pattern.compile("^(\\d+)\\.(\\d+)(?>\\.(\\d+)(.*))?");

    /** The major integer version */
    private final int major;
    /** A predicate that recognizes that a version string matches this major */
    private final Predicate<String> matcher;
    /** The URL of the server list file for this major version */
    private final String serverListUrl;
    /** A function that takes a full version string and produces the URL for the release notes of this version */
    private final Function<String, Optional<String>> releaseNotesUrlBuilder;

    public static XPlaneMajorVersion of(String version) {
        return Arrays.stream(values())
                .filter(v -> v.matcher.test(version))
                .findFirst()
                .orElseThrow();
    }

    private static Optional<String> xp12ReleaseNotes(String version) {
        {
            Matcher matcher = NEW_VERSION_PATTERN.matcher(version);
            if (matcher.matches()) {
                String minor = matcher.group(2);
                String patch = matcher.group(3);
                return Optional.of(String.format("https://www.x-plane.com/kb/x-plane-12-%s-%s-release-notes",
                        minor, patch));
            }
        }
        {
            Matcher matcher = LEGACY_VERSION_PATTERN.matcher(version);
            if (matcher.matches()) {
                return Optional.of(String.format("https://www.x-plane.com/kb/x-plane-12-00-release-notes/#%s",
                        version.replace(".", "")));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> xp11ReleaseNotes(String version) {
        Matcher matcher = LEGACY_VERSION_PATTERN.matcher(version);
        if (matcher.matches()) {
            String minor = matcher.group(2);
            return Optional.of(String.format("https://www.x-plane.com/kb/x-plane-11-%s-release-notes", minor));
        }
        return Optional.empty();
    }

    /** Returns the 4-digit spec string for this version, e.g. "1100" for X-Plane 11. */
    public String specString() {
        return String.format("%02d00", major);
    }

    public UpdateInformation getUpdateInformation() {
        return new UpdateInformation(this);
    }
}
