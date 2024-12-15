package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.XPlaneReleaseInfo;
import com.ogerardin.xplane.file.ServersFile;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;

/**
 * Information regarding the latest final and beta versions of a specific X-Plane major version.
 */
@Data
@Slf4j
public class UpdateInformation {

    private final static XPlaneReleaseInfo NO_VERSION = new XPlaneReleaseInfo("n/a", null);

    @NonNull
    private final XPlaneMajorVersion majorVersion;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ServersFileData data = loadData();

    @SneakyThrows
    private ServersFileData loadData() {
        // retrieve the URL of the file containing the update information
        String serverListUrl = majorVersion.getServerListUrl();
        if (serverListUrl == null) {
            log.error("Server list URL is unknown for version '{}'", majorVersion.name());
            return null;
        }
        final URL url = new URL(serverListUrl);
        ServersFile serversFile = new ServersFile(url);
        return serversFile.getData();
    }

    public XPlaneReleaseInfo getLatestBeta() {
        ServersFileData value = getData();
        if (value != null) {
            String betaVersion = value.getBetaVersion();
            if (betaVersion != null) {
                return new XPlaneReleaseInfo(betaVersion, majorVersion.getReleaseNotesUrlBuilder().apply(betaVersion));
            }
        }
        return NO_VERSION;
    }

    public XPlaneReleaseInfo getLatestFinal() {
        ServersFileData value = getData();
        if (value != null) {
            String finalVersion = value.getFinalVersion();
            if (finalVersion != null) {
                return new XPlaneReleaseInfo(finalVersion, majorVersion.getReleaseNotesUrlBuilder().apply(finalVersion));
            }
        }
        return NO_VERSION;
    }

}
