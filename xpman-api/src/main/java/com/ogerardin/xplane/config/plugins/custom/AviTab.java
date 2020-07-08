package com.ogerardin.xplane.config.plugins.custom;

import com.ogerardin.xplane.config.plugins.Plugin;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import static com.ogerardin.xplane.util.IntrospectionHelper.require;

@SuppressWarnings("unused")
@Slf4j
public class AviTab extends Plugin {

    private static final String XPLANE_FORUM_URL = "https://forums.x-plane.org/index.php?/files/file/44825-avitab-vr-compatible-tablet-with-pdf-viewer-moving-maps-and-more/";

    //TODO ability to manage charts

    public AviTab(Path folder) throws InstantiationException {
        super(folder, "AviTab: VR-compatible tablet with PDF viewer, moving maps and more");
        require(folder.endsWith("AviTab"));
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
