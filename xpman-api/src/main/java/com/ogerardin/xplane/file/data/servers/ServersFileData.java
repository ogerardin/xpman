package com.ogerardin.xplane.file.data.servers;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServersFileData extends XPlaneFileData {

    private String betaVersion;
    private String finalVersion;

//    private Map<Platform, URL> betaInstaller = new HashMap<>();
//    private Map<Platform, URL> finalInstaller = new HashMap<>();

    public ServersFileData(Header header) {
        super(header);
    }
}
