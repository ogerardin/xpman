package com.ogerardin.xplane.file.data.servers;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
public class ServersFileData extends XPlaneFileData {

//    private Map<Platform, URL> betaInstaller = new HashMap<>();
//    private Map<Platform, URL> finalInstaller = new HashMap<>();

    private Map<String, String> versionByType = new HashMap<>();

    public ServersFileData(Header header) {
        super(header);
    }

    public String getBetaVersion() {
        return versionByType.get(Version.TYPE_BETA);
    }

    public String getFinalVersion() {
        return versionByType.get(Version.TYPE_FINAL);
    }

    public void put(Version version) {
        versionByType.put(version.getType(), version.getVersion());
    }

    @Data
    public static class Version {
        public static final String TYPE_BETA = "BETA";
        public static final String TYPE_FINAL = "FINAL";

        private final String type;
        private final String version;
    }
}
