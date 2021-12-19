package com.ogerardin.xplane.aircrafts.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.PublicationChannel;
import com.ogerardin.xplane.Versioned;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.impl.RecommendedPluginsInspection;
import com.ogerardin.xplane.plugins.custom.AviTab;
import com.ogerardin.xplane.plugins.custom.TerrainRadar;
import com.ogerardin.xplane.util.GoogleDriveClient;
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
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
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

    public ZiboMod738(AcfFile acfFile) throws Exception {
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
        Pattern pattern = Pattern.compile("(.+ )*v([0-9a-zA-Z.]+)$");
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
                        "Facebook page", new URL("https://www.facebook.com/zibocommunity"),
                        "X-Plane forum", new URL("https://forums.x-plane.org/index.php?/forums/topic/138974-b737-800x-zibo-mod-info-installation-download-links"),
                        "Download page", channel.getUrl()
                ));
    }

    @Override
    public Inspections<Aircraft> getInspections(XPlane xPlane) {
        return super.getInspections(xPlane)
                .append(new RecommendedPluginsInspection<>(xPlane, AviTab.class, TerrainRadar.class));
    }


    /**
     * This class uses the ZiboMod Google Drive https://drive.google.com/drive/folders/0B-tdl3VvPeOOYm12Wm80V04wdDQ
     * to extract the latest version. Unfortunately something has changed lately and the access doesn't work anonymously
     * anymore. Prefer {@link ZiboUpdaterChannel}.
     */
    static class GoogleDriveChannel implements PublicationChannel {
        /**
         * The Google Drive folder ID of the folder containing published updates
         */
        private static final String ZIBO_FOLDER_ID = "0B-tdl3VvPeOOYm12Wm80V04wdDQ";

        public String getLatestVersion() throws IOException, GeneralSecurityException {
            // Full versions are published as a file B737-800X_<version>_full.zip, e.g. B737-800X_3_42_full.zip
            // Patches are published as incremental files B738X_<version>_<patch>.zip, e.g. B737-800X_3_42_10.zip

            // obtain list of files on the Google Drive
            GoogleDriveClient client = new GoogleDriveClient();
            List<File> files = client.getFiles(ZIBO_FOLDER_ID);

            // try to find file matching full version pattern and extract version
            Pattern pattern = Pattern.compile("B737-800X_([\\d_]+)_full.zip");
            Optional<String> maybeVersion = files.stream()
                    .map(File::getName)
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .findAny()
                    .map(matcher -> matcher.group(1));
            if (! maybeVersion.isPresent()) {
                log.warn("Failed to determine ZIBO major version from Google Drive");
                return null;
            }

            // try to find patches for this version, keep the max
            String version = maybeVersion.get();
            Pattern pattern2 = Pattern.compile("B738X_" + version + "_(\\d+).zip");
            OptionalInt maybePatch = files.stream()
                    .map(File::getName)
                    .map(pattern2::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .mapToInt(Integer::valueOf)
                    .max();

            version = version.replaceAll("_", ".");
            return maybePatch.isPresent() ? String.format("%s.%d", version, maybePatch.getAsInt()) : version;
        }

        @SneakyThrows
        @Override
        public URL getUrl() {
            return new URL("https://drive.google.com/drive/folders/" + GoogleDriveChannel.ZIBO_FOLDER_ID);
        }
    }

    /**
     * Uses the ZiboUpdater page to determine the lastest version.
     * We don't have structured data so we do some HTML scraping which may break anytime.
     */
    static class ZiboUpdaterChannel implements PublicationChannel {

        public static final String ZIBOUPDATER_URL = "https://ziboupdater.net/getzibo";

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
