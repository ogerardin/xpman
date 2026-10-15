package com.ogerardin.xplane.scenery.custom;

import com.ogerardin.xplane.scenery.SceneryPackage;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialized SceneryPackage that handles XPFR sceneries.<p>
 * See <a href="https://www.xpfr.org/?body=scene_accueil">www.xpfr.org</a>
 */
@SuppressWarnings("unused")
public class XpfrScenery extends SceneryPackage {

    private static final Pattern filePattern = Pattern.compile("((.+)\\.v\\.(\\d+-\\d+_\\(\\d+\\)(?:_\\d+)?))\\.txt");

    @Getter(lazy = true)
    private final String id = loadId();

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrScenery(Path folder) throws InstantiationException {
        super(folder);
        require(matchIdFile(folder).isPresent());
    }

    @SneakyThrows
    private Optional<Matcher> matchIdFile(Path folder) {
        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream
                    .map(path -> path.getFileName().toString())
                    .map(filePattern::matcher)
                    .filter(Matcher::matches)
                    .findAny();
        }
    }

    private String loadId() {
        return matchIdFile(getFolder()).map(matcher -> matcher.group(1)).orElse(null);
    }

    private String loadVersion() {
        return matchIdFile(getFolder()).map(matcher -> matcher.group(3)).orElse(null);
    }


    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "Scenery detailed sheet on xpfr.org",
                        Urls.url(String.format("https://www.xpfr.org/?body=scene_accueil&seek=%s", URLEncoder.encode(getId(), "UTF-8")))
                )
        );
    }

}
