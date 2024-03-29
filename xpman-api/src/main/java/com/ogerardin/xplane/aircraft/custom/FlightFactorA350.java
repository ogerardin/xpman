package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class FlightFactorA350 extends Aircraft {

    @Getter(lazy = true)
    private final String version = loadVersion();

    public FlightFactorA350(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile, "Flight Factor Airbus A350 XWB Advanced");
        IntrospectionHelper.require(
                getAcfName().equals("Airbus a350 XP11")
                        && getStudio().equals("FlightFactor")
        );
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        Map<String, URL> links = super.getLinks();
        links.put("Briefing", getAcfFile().getFile().resolveSibling("Briefing.pdf").toUri().toURL());
        return links;
    }

    @SneakyThrows
    private String loadVersion() {
        Path iniFile = getAcfFile().getFile().resolveSibling("a350.ini");
        try {
            List<String> lines = Files.readAllLines(iniFile);
            String versionLine = lines.get(0);
            Pattern pattern = Pattern.compile("([0-9]{2})([0-9]{2})([0-9]{2})");
            Matcher matcher = pattern.matcher(versionLine);
            if (matcher.matches()) {
                return String.format("%d.%d.%d",
                        Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2)),
                        Integer.parseInt(matcher.group(3))
                );
            }
        } catch (Exception ignored) {
        }
        return super.getVersion();
    }
}
