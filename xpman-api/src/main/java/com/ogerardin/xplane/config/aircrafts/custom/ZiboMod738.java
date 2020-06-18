package com.ogerardin.xplane.config.aircrafts.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.GoogleDriveClient;
import com.ogerardin.xplane.util.Maps;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ZiboMod738 extends Aircraft implements CustomAircraft {

    /** The Google Drive folder ID of the folder containing published updates */
    public static final String ZIBO_FOLDER_ID = "0B-tdl3VvPeOOYm12Wm80V04wdDQ";

    @Getter(lazy = true)
    private final String version = loadVersion();

    public ZiboMod738(AcfFile acfFile) throws InstantiationException {
        super(acfFile, "ZIBO Mod 737-800X");
        assertValid(acfFile.getNotes().startsWith("ZIBOmod"));
    }

    public String loadVersion() {
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
    public Map<LinkType, URL> getLinks() {
        return Maps.mapOf(
                LinkType.HOMEPAGE, new URL("https://www.facebook.com/zibocommunity"),
                LinkType.XPLANE_FORUM, new URL("https://forums.x-plane.org/index.php?/forums/topic/138974-b737-800x-zibo-mod-info-installation-download-links/")
        );
    }

    public String getLatestVersion() throws GeneralSecurityException, IOException {
        // The aircraft is published on a Google drive: https://drive.google.com/drive/folders/0B-tdl3VvPeOOYm12Wm80V04wdDQ
        // Full versions are published as a file B737-800X_<version>_full.zip, e.g. B737-800X_3_42_full.zip
        // Patches are published as invremental files B737-800X_<version>_<patch>.zip, e.g. B737-800X_3_42_10.zip
        GoogleDriveClient client = new GoogleDriveClient();
        List<File> files = client.getFiles(ZIBO_FOLDER_ID);
        Pattern pattern = Pattern.compile("B737-800X_([\\d_]+)_full.zip");
        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches()) {
                String g1 = matcher.group(1);
                String version = g1.replaceAll("_", ".");
                return version;
            }
        }
        return null;
    }
}
