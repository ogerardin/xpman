package com.ogerardin.xplane.plugins.custom;

import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneVariant;
import com.ogerardin.xplane.plugins.Plugin;
import com.ogerardin.xplane.update.PublicationUpdateProvider;
import com.ogerardin.xplane.update.UpdateProvider;
import java.util.Optional;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import com.ogerardin.xplane.util.Urls;
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

import static com.ogerardin.xplane.util.IntrospectionHelper.*;

/**
 * Specialization of {@link Plugin} for the X-IvAp IVAO network client, scraping latest version from the IVAO site.
 */
@SuppressWarnings("unused")
@Slf4j
public class XIvAp extends Plugin {

    private static final Map<XPlaneVariant, String> PLATFORM_NAME_MAP = Maps.mapOf(
            XPlaneVariant.MAC, "Mac OS",
            XPlaneVariant.WINDOWS, "Windows",
            XPlaneVariant.LINUX, "Linux"
    );

    private static final String IVAP_HOME_URL = "https://www.ivao.aero/software/ivap";
    private static final String X_IVAP_MANUAL_URL = "https://wiki.ivao.aero/en/home/devops/manuals/xivap";

    public XIvAp(XPlane xPlane, Path xplFile) throws InstantiationException {
        super(xPlane, xplFile, "X-IvAp", "Pilot client for the IVAO network");
        require(isXIvAp(xplFile));
    }

    private boolean isXIvAp(Path xplFile) {
        return xplFile.getFileName().toString().equals("X-IvAp-64.xpl");
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.mapOf(
                "IvAp home page", Urls.url(IVAP_HOME_URL),
                "X-IvAp manual", Urls.url(X_IVAP_MANUAL_URL)
        );
    }

    @Override
    protected Optional<UpdateProvider> loadUpdateProvider() {
        return super.loadUpdateProvider().or(() -> Optional.of(new PublicationUpdateProvider(this::getVersion, this::isEnabled) {
            public String getName() { return "IVAO"; }
            @SneakyThrows public URL getUrl() { return Urls.url(IVAP_HOME_URL); }
            protected String fetchLatestVersion() throws java.io.IOException { return loadLatestVersion(); }
        }));
    }

    private String loadLatestVersion() throws java.io.IOException {
        Document doc = Jsoup.connect(IVAP_HOME_URL).get();

        Pattern pattern = Pattern.compile("v([0-9.]++)[^\\r\\n]+Current");
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
