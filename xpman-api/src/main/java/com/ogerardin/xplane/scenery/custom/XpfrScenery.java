package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Specialized SceneryPackage that handles XPFR sceneries.<p>
 * See <a href="https://www.xpfr.org/?body=scene_accueil">www.xpfr.org</a>
 */
@SuppressWarnings("unused")
public class XpfrScenery extends SceneryPackage {

    private final Pattern FILE_PATTERN = Pattern.compile("(([A-Z]{4})\\.v\\.(\\d+)-(\\d+)_\\((\\d+)\\))\\.txt");

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrScenery(Path folder) throws InstantiationException {
        super(folder);
        IntrospectionHelper.require(hasXprfIdFile(folder));
    }

    @SneakyThrows
    private boolean hasXprfIdFile(Path folder) {
        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream
                    .map(path -> path.getFileName().toString())
                    .map(FILE_PATTERN::matcher)
                    .anyMatch(Matcher::matches);
        }
    }

    @SneakyThrows
    private String loadVersion() {
        Path folder = getFolder();
        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream
                    .map(path -> path.getFileName().toString())
                    .map(FILE_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .findAny()
                    .map(matcher -> matcher.group(1))
                    .orElse(null);
        }
    }


    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "Scenery detailed sheet on xpfr.org",
                        new URL(String.format("https://www.xpfr.org/?body=scene_accueil&seek=%s", URLEncoder.encode(getVersion(), "UTF-8")))
                )
        );
    }

}
