package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class AviTab extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = "https://forums.x-plane.org/index.php?/files/file/44825-avitab-vr-compatible-tablet-with-pdf-viewer-moving-maps-and-more/";

    //TODO ability to manage charts

    public AviTab(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "AviTab", "VR-compatible tablet with PDF viewer, moving maps and more",
                XPLANEORG_URL);
        IntrospectionHelper.require(isAviTab(xplFile));
    }

    private boolean isAviTab(Path xplFile) {
        return xplFile.getFileName().toString().equals("AviTab.xpl");
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return  Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "GitHub page", new URL("https://github.com/fpw/avitab"),
                        "Wiki", new URL("https://github.com/fpw/avitab/wiki")
                )
        );
    }

}
