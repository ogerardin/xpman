package com.ogerardin.xplane.aircrafts.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.XPlane;
import com.ogerardin.xplane.aircrafts.Aircraft;
import com.ogerardin.xplane.plugins.custom.AviTab;
import com.ogerardin.xplane.plugins.custom.TerrainRadar;
import com.ogerardin.xplane.inspection.Inspections;
import com.ogerardin.xplane.inspection.impl.RecommendedPluginsInspection;
import com.ogerardin.xplane.file.AcfFile;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@Slf4j
public class ZiboMod738 extends Aircraft {

    /**
     * The Google Drive folder ID of the folder containing published updates
     */
    private static final String ZIBO_FOLDER_ID = "0B-tdl3VvPeOOYm12Wm80V04wdDQ";

    @Getter(lazy = true)
    private final String version = loadVersion();

    @Getter(lazy = true)
    private final String latestVersion = loadLatestVersion();

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
                        "Download (Google drive)", new URL("https://drive.google.com/drive/folders/" + ZIBO_FOLDER_ID)
                ));
    }

    @SneakyThrows
    public String loadLatestVersion() {
        // The aircraft is published on a Google drive: https://drive.google.com/drive/folders/0B-tdl3VvPeOOYm12Wm80V04wdDQ
        // Full versions are published as a file B737-800X_<version>_full.zip, e.g. B737-800X_3_42_full.zip
        // Patches are published as incremental files B738X_<version>_<patch>.zip, e.g. B737-800X_3_42_10.zip

        // obtain list of files on the Google drive
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

    @Override
    public Inspections<Aircraft> getInspections(XPlane xPlane) {
        return super.getInspections(xPlane)
                .append(new RecommendedPluginsInspection<>(xPlane, AviTab.class, TerrainRadar.class));
    }
}
