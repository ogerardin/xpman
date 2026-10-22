package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialization of {@link Aircraft} for XPFR aircraft, which use a non-standard version file naming
 * convention. XPFR stores version information in text files named like {@code *-ID.v.VERSION.txt},
 * where VERSION is in the format {@code N-N_(N)_N}. This class parses that pattern to extract the
 * aircraft ID and version, and provides a link to the aircraft's detail page on xpfr.org.
 * <p>
 * Recognition is based on either the studio field being "XPFR" or the presence of a matching version file.
 */
@SuppressWarnings("unused")
public class XpfrAircraft extends Aircraft {

    @SuppressWarnings("java:S8786") // Retain legacy hyphen matching; input is a single bounded local filename.
    private static final Pattern filePattern = Pattern.compile(".+-(([A-Z0-9\\-]++)\\.v\\.(\\d++-\\d++_\\(\\d++\\)(?:_\\d++)?))\\.txt");

    @Getter(lazy = true)
    private final String id = loadId();

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrAircraft(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile);
        // not all XPFR aircraft have "XPFR" as studio :(
        require(getStudio().equals("XPFR") || getId() != null);
    }

    @SneakyThrows
    private Optional<Matcher> matchIdFile() {
        Path folder = getAcfFile().getFile().getParent();
        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream
                    .map(path -> filePattern.matcher(path.getFileName().toString()))
                    .filter(Matcher::matches)
                    .max(Comparator.comparing(matcher -> matcher.group(1)));
        }
    }

    private String loadId() {
        return matchIdFile().map(matcher -> matcher.group(1)).orElse(null);
    }

    private String loadVersion() {
        return matchIdFile().map(matcher -> matcher.group(3)).orElse(null);
    }


    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "Aircraft detailed sheet on xpfr.org",
                        Urls.url(String.format("https://www.xpfr.org/?body=aero_accueil&seek=%s", URLEncoder.encode(getId(), StandardCharsets.UTF_8)))
                )
        );
    }

}
