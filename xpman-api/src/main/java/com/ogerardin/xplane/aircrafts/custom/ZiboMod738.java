package com.ogerardin.xplane.aircrafts.custom;

import com.ogerardin.xplane.PublicationChannel;
import com.ogerardin.xplane.Versioned;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.impl.RecommendedPluginsInspection;
import com.ogerardin.xplane.plugins.custom.AviTab;
import com.ogerardin.xplane.plugins.custom.TerrainRadar;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specialized {@link Aircraft} class to handle the Zibo Mod B737-800X.
 */
@SuppressWarnings("unused")
@Slf4j
public class ZiboMod738 extends Aircraft implements Versioned {

    private final PublicationChannel channel = new ZiboUpdaterChannel();

    @Getter(lazy = true)
    private final String version = loadVersion();

    @Getter(lazy = true)
    private final String latestVersion = loadLatestVersion();

    @SneakyThrows
    private String loadLatestVersion() {
        return channel.getLatestVersion();
    }

    public ZiboMod738(AcfFile acfFile) throws InstantiationException {
        super(acfFile);
        IntrospectionHelper.require(getNotes().startsWith("ZIBOmod"));
    }

    @Override
    public String getName() {
        return "ZIBO Mod " + getAcfName();
    }

    private String loadVersion() {
        // try version.txt file otherwise fallback to notes field
        try {
            Path versionFile = getAcfFile().getFile().resolveSibling("version.txt");
            return Files.readAllLines(versionFile).get(0);
        } catch (IOException e) {
            return loadVersionFromNotes();
        }

    }

    /**
     * Apparently less reliable than version.txt file
     */
    private String loadVersionFromNotes() {
        String notes = getNotes();
        Pattern pattern = Pattern.compile(".+v([0-9a-zA-Z.]+)$");
        Matcher matcher = pattern.matcher(notes);
        if (! matcher.matches()) {
            return super.getVersion();
        }
        return matcher.group(2);
    }

    @SneakyThrows
    @Override
    public Map<String, URL> getLinks() {
        return Maps.merge(
                super.getLinks(),
                Maps.mapOf(
                        "ZIBO community on Facebook", new URL("https://www.facebook.com/zibocommunity"),
                        "ZIBO mod forum on X-Plane.org", new URL("https://forums.x-plane.org/index.php?/forums/topic/138974-b737-800x-zibo-mod-info-installation-download-links"),
                        "Download page (" + channel.getName() + ")", channel.getUrl()
                ));
    }

    @Override
    public Inspections<Aircraft> getInspections(XPlane xPlane) {
        return super.getInspections(xPlane)
                .append(new RecommendedPluginsInspection<>(xPlane, AviTab.class, TerrainRadar.class));
    }

    /**
     * Uses the ZiboUpdater page to determine the lastest version.
     * We don't have structured data so we do some HTML scraping which may break anytime.
     */
    static class ZiboUpdaterChannel implements PublicationChannel {

        public static final String ZIBOUPDATER_URL = "https://ziboupdater.net/getzibo";

        @Override
        public String getName() {
            return "Zibo Updater";
        }

        @SneakyThrows
        @Override
        public String getLatestVersion() {
            Document doc = Jsoup.connect(ZIBOUPDATER_URL).get();

            final String majorVersion = extractVersion(doc, "#header a:contains(Full Release)");
            final String patchVersion = extractVersion(doc, "#header a:contains(Patch)");
            return (patchVersion != null) ? patchVersion : majorVersion;
        }

        private String extractVersion(Document doc, String selector) {
            Element element = doc.select(selector).first();
            if (element == null) {
                log.warn("Failed to locate '{}' in {}} ", selector, ZIBOUPDATER_URL);
                return null;
            }
            String text = element.text();
            Pattern pattern = Pattern.compile("\\D+ ([\\d.]+)");
            Matcher matcher = pattern.matcher(text);
            if (! matcher.matches()) {
                log.warn("Failed to parse version: " + text);
                return null;
            }
            return matcher.group(1);
        }

        @SneakyThrows
        @Override
        public URL getUrl() {
            return new URL(ZIBOUPDATER_URL);
        }
    }
}
