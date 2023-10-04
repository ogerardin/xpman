package com.ogerardin.xplane;

import com.ogerardin.xplane.laminar.UpdateInformation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
            version -> String.format("https://www.x-plane.com/kb/x-plane-12-00-release-notes/#%s", version.replace(".", ""))
    ),

    OTHER(0, version -> true, null, null);

    private static String minor(String version) {
        return Pattern.compile("^(\\d+)\\.(\\d+)(.*)").matcher(version).group(2);
    }

    private final int major;
    private final Predicate<String> matcher;
    private final String serverListUrl;
    private final Function<String, String> releaseNotesUrlBuilder;

    public static XPlaneMajorVersion of(String version) {
        return Arrays.stream(values())
                .filter(v -> v.matcher.test(version))
                .findFirst()
                .orElse(OTHER);
    }

    /** Returns the 4-digit spec string for this version, e.g. "1100" for X-Plane 11. */
    public String specString() {
        return String.format("%02d00", major);
    }

    public UpdateInformation getUpdateInformation() {
        return new UpdateInformation(this);
    }
}
