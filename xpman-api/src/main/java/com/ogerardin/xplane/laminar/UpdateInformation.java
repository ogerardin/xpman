package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.file.ServersFile;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Optional;

@Data
@Slf4j
public class UpdateInformation {

    @NonNull
    private final XPlaneMajorVersion majorVersion;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ServersFileData data = loadData();

    @SneakyThrows
    private ServersFileData loadData() {
        String serverListUrl = majorVersion.getServerListUrl();
        if (serverListUrl == null) {
            log.error("Server list URL is unknown for version '{}'", majorVersion.name());
            return null;
        }
        final URL url = new URL(serverListUrl);
        ServersFile serversFile = new ServersFile(url);
        return serversFile.getData();
    }

    public String getLatestBeta() {
        return Optional.ofNullable(getData()).map(ServersFileData::getBetaVersion).orElse("n/a");
    }

    public String getLatestFinal() {
        return Optional.ofNullable(getData()).map(ServersFileData::getFinalVersion).orElse("n/a");
    }


}
