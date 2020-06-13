package com.ogerardin.xplane.config.aircrafts.custom;

import com.google.api.services.drive.model.File;
import com.ogerardin.xplane.config.LinkType;
import com.ogerardin.xplane.config.aircrafts.Aircraft;
import com.ogerardin.xplane.file.AcfFile;
import com.ogerardin.xplane.util.GoogleDriveClient;
import com.ogerardin.xplane.util.Maps;
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

    public static final String ZIBO_FOLDER_ID = "0B-tdl3VvPeOOYm12Wm80V04wdDQ";

    public ZiboMod738(AcfFile acfFile) {
        super(acfFile, "ZIBO Mod 737-800X");
        if (! acfFile.getNotes().startsWith("ZIBOmod")) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getVersion() {
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
