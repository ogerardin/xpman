package com.ogerardin.xplane.aircraft.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.PublicationChannel;
import com.ogerardin.xplane.Versioned;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircraft.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.inspection.InspectionResult;
import com.ogerardin.xplane.inspection.impl.RecommendedPluginsInspection;
import com.ogerardin.xplane.plugins.custom.AviTab;
import com.ogerardin.xplane.plugins.custom.TerrainRadar;
import com.ogerardin.xplane.util.GoogleDriveClient;
import com.ogerardin.xplane.util.IntrospectionHelper;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static final RecommendedPluginsInspection RECOMMENDED_PLUGINS_INSPECTION
            = new RecommendedPluginsInspection(AviTab.class, TerrainRadar.class);

    private final PublicationChannel channel = new GoogleDriveChannel();

    @Getter(lazy = true)
    private final String version = loadVersion();

    @Getter(lazy = true)
    private final String latestVersion = loadLatestVersion();

    @SneakyThrows
    private String loadLatestVersion() {
        return channel.getLatestVersion();
    }

    public ZiboMod738(XPlane xPlane, AcfFile acfFile) throws InstantiationException {
        super(xPlane, acfFile);
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
    public InspectionResult inspect() {
        return super.inspect()
                .append(RECOMMENDED_PLUGINS_INSPECTION.inspect(getXPlane()));

    }


    /**
     * This class uses the ZiboMod Google Drive https://drive.google.com/drive/folders/1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK
     * to extract the latest version.
     * Note: there is also a torrents versrion; https://drive.google.com/drive/folders/12ggG4G1c0h_EIDgIaQAmU9bnuUWSOLrc
     */
    static class GoogleDriveChannel implements PublicationChannel {
        /** The Google Drive folder ID of the folder containing published updates */
        private static final String ZIBO_FOLDER_ID = "1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK";

        @Override
        public String getName() {
            return "Google Drive";
        }

        public String getLatestVersion() throws Exception {
            // Full versions are published as a file B737-800X_<version>_full.zip, e.g. B737-800X_3_42_full.zip
            // Patches are published as incremental files B738X_<version>_<patch>.zip, e.g. B737-800X_3_42_10.zip

            // obtain list of files on the Google Drive
            GoogleDriveClient client = new GoogleDriveClient();
            List<File> files = client.getFiles(ZIBO_FOLDER_ID);

            // try to find file matching full version pattern and extract version
            Pattern versionPattern = Pattern.compile("B737-800X_([\\d_]+)_full.zip");
            Optional<String> maybeVersion = files.stream()
                    .map(File::getName)
                    .map(versionPattern::matcher)
                    .filter(Matcher::matches)
                    .findAny()
                    .map(matcher -> matcher.group(1));
            if (maybeVersion.isEmpty()) {
                log.warn("Failed to determine ZIBO version from Google Drive");
                return null;
            }

            // try to find patches for this version, keep the max
            String version = maybeVersion.get();
            Pattern PatchPattern = Pattern.compile("B738X_" + version + "_(\\d+).zip");
            OptionalInt maybePatch = files.stream()
                    .map(File::getName)
                    .map(PatchPattern::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .mapToInt(Integer::valueOf)
                    .max();

            version = version.replace("_", ".");
            return maybePatch.isPresent() ? String.format("%s.%d", version, maybePatch.getAsInt()) : version;
        }

        @SneakyThrows
        @Override
        public URL getUrl() {
            return new URL("https://drive.google.com/drive/folders/" + GoogleDriveChannel.ZIBO_FOLDER_ID);
        }
    }

}
