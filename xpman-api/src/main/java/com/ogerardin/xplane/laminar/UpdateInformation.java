package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.IllegalOperation;
import com.ogerardin.xplane.XPlaneMajorVersion;
import com.ogerardin.xplane.file.ServersFile;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.*;

import java.io.InputStream;
import java.net.URL;

import static java.nio.charset.StandardCharsets.US_ASCII;

@Data
public class UpdateInformation {

    @NonNull
    private final XPlaneMajorVersion majorVersion;

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ServersFileData data = loadData();

    @SneakyThrows
    private ServersFileData loadData() {
        if (majorVersion.getServerListUrl() == null) {
            throw new IllegalOperation("Server list URL unknown for version " + majorVersion.name());
        }
        final URL url = new URL(majorVersion.getServerListUrl());
        try (InputStream inputStream = url.openStream()) {
            final byte[] bytes = inputStream.readAllBytes();
            String contents = new String(bytes, US_ASCII);
            ServersFile serversFile = new ServersFile();
            return serversFile.parse(contents);
        }

    }

    public String getLatestBeta() {
        return getData().getBetaVersion();
    }

    public String getLatestFinal() {
        return getData().getFinalVersion();
    }


}
