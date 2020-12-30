package com.ogerardin.xplane.aircrafts.custom;

import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
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

@SuppressWarnings("unused")
public class XpfrAircraft extends Aircraft {

    private final Pattern FILE_PATTERN = Pattern.compile(".+-(([A-Z\\-]+)\\.v\\.(\\d+)-(\\d+)_\\((\\d+)\\))\\.txt");

    @Getter(lazy = true)
    private final String version = loadVersion();

    public XpfrAircraft(AcfFile acfFile) throws InstantiationException {
        super(acfFile);
        // not all XPFR aircrafts have "XPFR" as studio :(
        //        IntrospectionHelper.require(getStudio().equals("XPFR"));
        IntrospectionHelper.require(getVersion() != null);
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
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "Aircraft detailed sheet on xpfr.org",
                        new URL(String.format("https://www.xpfr.org/?body=aero_accueil&seek=%s", URLEncoder.encode(getVersion(), "UTF-8")))
                )
        );
    }

}
