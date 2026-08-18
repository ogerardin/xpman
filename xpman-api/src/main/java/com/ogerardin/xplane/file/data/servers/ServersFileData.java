package com.ogerardin.xplane.file.data.servers;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model for parsed server list files containing version information.
 *
 * <p>Server list files contain information about available X-Plane versions
 * (beta and final) for different platforms. This data is used to check for updates.</p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * ServersFileData data = serversFile.getData();
 * String betaVersion = data.getBetaVersion();
 * String finalVersion = data.getFinalVersion();
 * }</pre>
 *
 * @author Olivier G.
 * @see com.ogerardin.xplane.file.ServersFile
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ServersFileData extends XPlaneFileData {

    /**
     * Map of version types (BETA, FINAL) to version strings.
     */
    private Map<String, String> versionByType = new HashMap<>();

    /**
     * Create a ServersFileData instance.
     *
     * @param header the file header
     */
    public ServersFileData(Header header) {
        super(header);
    }

    /**
     * Get the beta version string.
     *
     * @return the beta version, or null if not present
     */
    public String getBetaVersion() {
        return versionByType.get(Version.TYPE_BETA);
    }

    /**
     * Get the final (stable) version string.
     *
     * @return the final version, or null if not present
     */
    public String getFinalVersion() {
        return versionByType.get(Version.TYPE_FINAL);
    }

    /**
     * Add a version entry.
     *
     * @param version the version to add
     */
    public void put(Version version) {
        versionByType.put(version.getType(), version.getVersion());
    }

    /**
     * Represents a version entry with type and version string.
     */
    @Data
    public static class Version {
        /**
         * Beta version type constant.
         */
        public static final String TYPE_BETA = "BETA";

        /**
         * Final (stable) version type constant.
         */
        public static final String TYPE_FINAL = "FINAL";

        private final String type;
        private final String version;
    }
}
