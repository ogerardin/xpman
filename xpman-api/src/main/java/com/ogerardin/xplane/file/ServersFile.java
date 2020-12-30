package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.grammar.ServersFileParser;
import com.ogerardin.xplane.file.data.servers.ServersFileData;

public class ServersFile extends XPlaneFile<ServersFileParser, ServersFileData> {

    public ServersFile() {
        super(null, ServersFileParser.class);
    }

}
