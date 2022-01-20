package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

public class ServersFileParser extends XPlaneFileParserBase<ServersFileData> implements StringParser<ServersFileData> {

    static final String REQUIRED_TYPE = "SERVERS";

    @SuppressWarnings("unchecked")
    @Override
    public Parser XPlaneFile() {
        return Header(REQUIRED_TYPE)
                .seq(Newline().star())
                .seq(VersionDecl().plus())
                .seq(JunkLine().star())

                .map((List<Object> input) -> {
                    ServersFileData data = new ServersFileData((Header) input.get(0));
                    List<ServersFileData.Version> versions = (List<ServersFileData.Version>) input.get(2);
                    versions.forEach(data::put);
                    return data;
                });
    }

    Parser VersionDecl() {
        return VersionType()
                .seq(of(" X-Plane "))
                .seq(Value())
                .seq(Newline())

                .map((List<Object> input)
                        -> new ServersFileData.Version((String) input.get(0), (String) input.get(2)));
    }

    private Parser VersionType() {
        return of("BETA").or(of("FINAL")).or(of("FULL"));
    }

    Parser Value() {
        return noneOf("\r\n").plus().flatten();
    }


}
