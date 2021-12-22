package com.ogerardin.xplane.file;

import com.ogerardin.xplane.file.data.servers.ServersFileData;
import com.ogerardin.xplane.file.parboiled.ParboiledParser;
import com.ogerardin.xplane.file.parboiled.ServersFileParser;

public class ServersFile extends XPlaneFile<ServersFileData> {

    public ServersFile() {
        super(null, new ParboiledParser<>(ServersFileParser.class));
    }

}
