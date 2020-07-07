package com.ogerardin.xplane.config.aircrafts.custom;

import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

@SuppressWarnings("unused")
public class XpfrAircraft extends Aircraft {

    private final Pattern FILE_PATTERN = Pattern.compile(".+-(([A-Z]{4})\\.v\\.(\\d+)-(\\d+)_\\((\\d+)\\))\\.txt");

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrAircraft(AcfFile acfFile) throws InstantiationException {
        super(acfFile);
        require(getStudio().equals("XPFR"));
    }

    @SneakyThrows
    private String loadVersion() {
        Path folder = getAcfFile().getFile().getParent();
        String version = Files.list(folder)
                .map(path -> FILE_PATTERN.matcher(path.getFileName().toString()))
                .filter(Matcher::matches)
                .findAny()
                .map(matcher -> matcher.group(1))
                .orElse(null);
        return version;
    }


    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "Homepage", new URL("http://www.xpfr.org")
        );
    }

}
