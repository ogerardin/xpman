package com.ogerardin.xplane.laminar;

import com.ogerardin.xplane.file.ServersFile;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.parboiled.common.FileUtils;

import java.net.URL;

import static java.nio.charset.StandardCharsets.US_ASCII;

@UtilityClass
public class UpdateInformation {

    private static final String SERVERS_URL = "http://lookup-a.x-plane.com/_lookup_11_/server_list_11.txt";

    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private final ServersFileData data = loadData();

    @SneakyThrows
    private ServersFileData loadData() {
        final URL url = new URL(SERVERS_URL);
        //TODO FileUtils is from parboiled: use something else!
        final byte[] bytes = FileUtils.readAllBytes(url.openStream());
        String contents = new String(bytes, US_ASCII);

        ServersFile serversFile = new ServersFile();
        return serversFile.parse(contents);
    }

    public String getLatestBeta() {
        return getData().getBetaVersion();
    }

    public String getLatestFinal() {
        return getData().getFinalVersion();
    }


}
