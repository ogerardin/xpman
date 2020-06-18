package com.ogerardin.xplane.config;

import lombok.Getter;

public enum LinkType {
    HOMEPAGE("Homepage"),
    SUPPORT("Support"),
    DOWNLOAD("Download page"),
    XPLANE_FORUM("X-Plane forum");

    @Getter
    private final String label;

    LinkType(String label) {
        this.label = label;
    }
}
