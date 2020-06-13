package com.ogerardin.xplane.config.plugins.custom;

import com.ogerardin.xplane.config.plugins.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class TerrainRadar extends Plugin implements CustomPlugin {

    public TerrainRadar(Path folder) {
        super(folder, "Terrain Radar");
        if (! folder.endsWith("TerrainRadar")) {
            throw new IllegalArgumentException();
        }
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
}
