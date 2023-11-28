package com.ogerardin.xplane.aircraft.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dmax3D_EF2000 extends Aircraft {

    private final Pattern FILE_PATTERN = Pattern.compile("ef2000_v(\\d)(\\d)");

    @Getter(lazy = true)
    private final String version = loadVersion();

    @SneakyThrows
    private String loadVersion() {
        String folder = getAcfFile().getFile().getParent().getFileName().toString();
        Matcher m = FILE_PATTERN.matcher(folder);
        return m.matches() ? m.group(1) + "." + m.group(2) : null;
    }

    public Dmax3D_EF2000(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile, "Dmax3D Eurofighter Typhoon");
        IntrospectionHelper.require(getStudio().equals("dmax3d.com") && getAcfName().equals("Eurofighter Typhoon"));
    }

    @SneakyThrows
    public Map<String, URL> getLinks() {
        return Maps.merge(super.getLinks(),
                Maps.mapOf("Eurofighter Typhoon on dmax3d.com", new URL("http://www.dmax3d.com/dmax3d/eurofighter.html")
                )
        );
    }


}
