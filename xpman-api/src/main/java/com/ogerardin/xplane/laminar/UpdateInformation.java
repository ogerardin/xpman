package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.file.ServersFile;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.US_ASCII;

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
        try (InputStream inputStream = url.openStream()) {
            final byte[] bytes = inputStream.readAllBytes();
            String contents = new String(bytes, US_ASCII);
            ServersFile serversFile = new ServersFile();
            return serversFile.parse(contents);
        }

    }

    public String getLatestBeta() {
        return Optional.ofNullable(getData()).map(ServersFileData::getBetaVersion).orElse("n/a");
    }

    public String getLatestFinal() {
        return Optional.ofNullable(getData()).map(ServersFileData::getFinalVersion).orElse("n/a");
    }


}
