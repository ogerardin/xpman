package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class AviTab extends Plugin {

    private static final String XPLANE_FORUM_URL = "https://forums.x-plane.org/index.php?/files/file/44825-avitab-vr-compatible-tablet-with-pdf-viewer-moving-maps-and-more/";

    //TODO ability to manage charts

    public AviTab(Path xplFile) throws InstantiationException {
        super(xplFile, "AviTab: VR-compatible tablet with PDF viewer, moving maps and more");
        IntrospectionHelper.require(xplFile.equals("AviTab.xpl"));
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "X-Plane forum", new URL(XPLANE_FORUM_URL),
                "GitHub page", new URL("https://github.com/fpw/avitab"),
                "Wiki", new URL("https://github.com/fpw/avitab/wiki")
        );
    }

}
