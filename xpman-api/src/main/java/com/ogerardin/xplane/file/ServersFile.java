package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.servers.ServersFileData;
import com.ogerardin.xplane.file.petitparser.ServersFileParser;

import java.net.URISyntaxException;
import java.net.URL;

/** Represents a "current versions" file as returned by e.g.  <a href="https://lookup.x-plane.com/_lookup_11_/server_list_11.txt">https://lookup.x-plane.com/_lookup_11_/server_list_11.txt</a> */
public class ServersFile extends XPlaneFile<ServersFileData> {

    public ServersFile(URL url) throws URISyntaxException {
        super(url, new ServersFileParser());
    }

    @Override
    public String getFileSpecVersion() {
        return null;
    }
}
