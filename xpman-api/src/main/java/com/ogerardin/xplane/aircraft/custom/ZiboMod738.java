package com.ogerardin.xplane.aircraft.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.PublicationChannel;
import com.ogerardin.xplane.Versioned;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.XPlaneMajorVersion;
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

    private final PublicationChannel channel = new GoogleDriveChannel(getXPlane().getMajorVersion());

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
    public static class GoogleDriveChannel implements PublicationChannel {
        /** The Google Drive folder ID of the folder containing published updates */
        private static final String ZIBO_FOLDER_ID = "1RHz4PQqWNGGpVG9GaHr84kuGs8LM2xyK";

        private final XPlaneMajorVersion majorVersion;

        public GoogleDriveChannel(XPlaneMajorVersion majorVersion) {
            this.majorVersion = majorVersion;
        }

        @Override
        public String getName() {
            return "Google Drive";
        }

        public String getLatestVersion() throws Exception {
            // Full versions are published as a file B737-800X_<version>_full.zip, e.g. B737-800X_3_42_full.zip
            // Patches are published as incremental files B738X_<version>_<patch>.zip, e.g. B737-800X_3_42_10.zip
            // Since the Drive folder was reorganized into XP11/XP12 subfolders, release files live inside the
            // subfolder named "XP<major>"; we fall back to the root listing for the legacy flat layout.

            // obtain list of files on the Google Drive
            GoogleDriveClient client = new GoogleDriveClient();
            List<File> files = client.getFiles(ZIBO_FOLDER_ID);

            // descend into the version-specific subfolder if present (new layout)
            String subfolderName = "XP" + majorVersion.getMajor();
            Optional<File> subfolder = files.stream()
                    .filter(f -> subfolderName.equals(f.getName()))
                    .findAny();
            if (subfolder.isPresent()) {
                files = client.getFiles(subfolder.get().getId());
            }

            return extractLatestVersion(files);
        }

        /**
         * Pure extraction of the latest Zibo mod version string from a list of Google Drive files.
         * <p>
         * Handles both the legacy flat naming (e.g. {@code B737-800X_3_42_full.zip} / {@code B738X_3_42_10.zip})
         * and the reorganized per-sim naming (e.g. {@code B737-800X_XP12_4_05_full.zip} /
         * {@code B738X_XP12_4_05_35.zip}), where the {@code XPnn_} prefix is optional.
         *
         * @param files the release files (full archive + incremental patches)
         * @return the latest version string (e.g. {@code "4.05.35"}) with underscores replaced by dots and
         *         the highest patch number appended, or {@code null} if no full archive is present
         */
        public static String extractLatestVersion(List<File> files) {
            // full version pattern: B737-800X_[XP\d+_]<version>_full.zip
            Pattern versionPattern = Pattern.compile("B737-800X_(?:XP\\d+_)?([a-zA-Z0-9_]+)_full\\.zip");
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

            // patch pattern: B738X_[XP\d+_]<version>_<patch>.zip — keep the highest patch number
            String version = maybeVersion.get();
            Pattern patchPattern = Pattern.compile("B738X_(?:XP\\d+_)?([a-zA-Z0-9_]+)_(\\d+)\\.zip");
            OptionalInt maybePatch = files.stream()
                    .map(File::getName)
                    .map(patchPattern::matcher)
                    .filter(Matcher::matches)
                    .filter(m -> version.equals(m.group(1)))
                    .mapToInt(m -> Integer.parseInt(m.group(2)))
                    .max();

            String dotted = version.replace("_", ".");
            return maybePatch.isPresent() ? String.format("%s.%d", dotted, maybePatch.getAsInt()) : dotted;
        }

        @SneakyThrows
        @Override
        public URL getUrl() {
            return new URL("https://drive.google.com/drive/folders/" + GoogleDriveChannel.ZIBO_FOLDER_ID);
        }
    }

}
