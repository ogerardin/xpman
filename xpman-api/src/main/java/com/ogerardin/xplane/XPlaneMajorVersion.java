package com.ogerardin.xplane;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum XPlaneMajorVersion {
    XP11(11,"http://lookup-a.x-plane.com/_lookup_11_/server_list_11.txt"),

    XP12(12, "https://lookup.x-plane.com/_lookup_12_/server_list_12.txt"),

    OTHER(0, null);

    private final int major;
    private final String serverListUrl;

    public static XPlaneMajorVersion of(String version) {
        if (version.startsWith("11")) {
            return XP11;
        }
        else if (version.startsWith("12")) {
            return XP12;
        }
        else {
            return OTHER;
        }
    }

    public String specString() {
        return String.format("%02d00", major);
    }
}
