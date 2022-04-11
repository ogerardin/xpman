package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Slf4j
public class XIvAp extends Plugin {

    private static final Map<XPlaneVariant, String> PLATFORM_NAME_MAP = Maps.mapOf(
            XPlaneVariant.MAC, "Mac OS",
            XPlaneVariant.WINDOWS, "Windows",
            XPlaneVariant.LINUX, "Linux"
    );

    private static final String IVAP_HOME_URL = "https://www.ivao.aero/softdev/ivap.aspx";
    private static final String X_IVAP_MANUAL_URL = "https://wiki.ivao.aero/en/home/devops/manuals/xivap";

    public XIvAp(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "X-IvAp", "Pilot client for the IVAO network");
        IntrospectionHelper.require(isXIvAp(xplFile));
    }

    private boolean isXIvAp(Path xplFile) {
        return xplFile.getFileName().toString().equals("X-IvAp-64.xpl");
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "IvAp home page", new URL(IVAP_HOME_URL),
                "X-IvAp manual", new URL(X_IVAP_MANUAL_URL)
        );
    }

    @SneakyThrows
    @Override
    public String getLatestVersion() {
        Document doc = Jsoup.connect(IVAP_HOME_URL).get();

        Pattern pattern = Pattern.compile("v([0-9.]+).+Current");
        String platformName = PLATFORM_NAME_MAP.get(getXPlane().getVariant());
        return doc.select("div#dl td:matches(X-IvAp for X-Plane.*\\(" + platformName + "\\))").stream()
                .findFirst()
                .map(Element::parent)
                .map(element -> element.selectFirst("a"))
                .map(Element::text)
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .orElse(null);
    }
}
