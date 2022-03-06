package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.plugins.XPlaneOrgPlugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@Slf4j
public class TerrainRadar extends XPlaneOrgPlugin {

    private static final String XPLANEORG_URL = "https://forums.x-plane.org/index.php?/files/file/37864-terrain-radar-vertical-situation-display/";


    public TerrainRadar(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "Terrain Radar", null, XPLANEORG_URL);
        IntrospectionHelper.require(isTerrainRadar(xplFile));
    }

    private boolean isTerrainRadar(Path xplFile) {
        return xplFile.getParent().endsWith("TerrainRadar/64");
    }

    @Override
    public String getVersion() {
        Path readme = getXplFile().getParent().resolveSibling("readme.txt");
        String firstLine;
        try (Stream<String> lines = Files.lines(readme)) {
            firstLine = lines.findFirst().orElse("");
        } catch (IOException e) {
            return super.getVersion();
        }
        Pattern pattern = Pattern.compile("Terrain radar plugin v([\\d\\.]+).*");
        Matcher matcher = pattern.matcher(firstLine);
        if (! matcher.matches()) {
            return super.getVersion();
        }
        return matcher.group(1);
    }

}
