package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class XpfrAircraft extends Aircraft {

    private final Pattern FILE_PATTERN = Pattern.compile(".+-(([A-Z0-9\\-]+)\\.v\\.(\\d+)-(\\d+)_\\((\\d+)\\))\\.txt");

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrAircraft(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile);
        // not all XPFR aircraft have "XPFR" as studio :(
        IntrospectionHelper.require(getStudio().equals("XPFR") || getVersion() != null);
    }

    @SneakyThrows
    private String loadVersion() {
        Path folder = getAcfFile().getFile().getParent();
        try (Stream<Path> pathStream = Files.list(folder)) {
            return pathStream
                    .map(path -> FILE_PATTERN.matcher(path.getFileName().toString()))
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .max(Comparator.naturalOrder())
                    .orElse(null);
        }
    }


    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "Aircraft detailed sheet on xpfr.org",
                        new URL(String.format("https://www.xpfr.org/?body=aero_accueil&seek=%s", URLEncoder.encode(getVersion(), "UTF-8")))
                )
        );
    }

}
