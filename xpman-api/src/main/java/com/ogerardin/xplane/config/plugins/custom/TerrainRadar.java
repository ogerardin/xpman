package com.ogerardin.xplane.config.plugins.custom;

import com.ogerardin.xplane.config.plugins.Plugin;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ogerardin.xplane.util.IntrospectionHelper.assertTrue;

@SuppressWarnings("unused")
public class TerrainRadar extends Plugin {

    public TerrainRadar(Path folder) throws InstantiationException {
        super(folder, "Terrain Radar");
        assertTrue(folder.endsWith("TerrainRadar"));
    }

    @Override
    public String getVersion() {
        Path readme = getFolder().resolve("readme.txt");
        String firstLine;
        try {
            //noinspection OptionalGetWithoutIsPresent
            firstLine = Files.lines(readme).findFirst().get();
        } catch (IOException e) {
            return super.getVersion();
        }
        Pattern pattern = Pattern.compile("Terrain radar plugin v([\\d\\.]+).*");
        Matcher matcher = pattern.matcher(firstLine);
        if (! matcher.matches()) {
            return super.getVersion();
        }
        String version = matcher.group(1);
        return version;
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "X-Plane forum", new URL("https://forums.x-plane.org/index.php?/files/file/37864-terrain-radar-vertical-situation-display")
        );
    }

}
