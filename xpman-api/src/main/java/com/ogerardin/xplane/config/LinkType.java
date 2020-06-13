package com.ogerardin.xplane.config;

import lombok.Data;

@Data
public class LinkType {

    private final String name;

    public static final LinkType HOMEPAGE = new LinkType("Homepage");
    public static final LinkType DOWNLOAD = new LinkType("Download page");
    public static final LinkType XPLANE_FORUM = new LinkType("X-Plane forum");

}
