package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.servers.ServersFileData;
import com.ogerardin.xplane.file.petitparser.ServersFileParser;

/** Represents a "current versions" file as returned by http://lookup-a.x-plane.com/_lookup_11_/server_list_11.txt */
public class ServersFile extends XPlaneFile<ServersFileData> {

    public ServersFile() {
        super(null, new ServersFileParser());
    }

}
