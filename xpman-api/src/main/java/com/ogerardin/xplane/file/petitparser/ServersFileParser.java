package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import org.petitparser.parser.Parser;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

public class ServersFileParser extends XPlaneFileParserBase<ServersFileData> implements StringParser<ServersFileData> {

    static final String REQUIRED_TYPE = "SERVERS";

    @Override
    public Parser XPlaneFile() {
        return Header(REQUIRED_TYPE)
                .map(ServersFileData::new)
                .seq(VersionDecl().seq(Newline())
                        .or(JunkLine()))
                .star();
    }

    Parser VersionDecl() {
        return of("BETA").seq(WhiteSpace()).seq(of("X-Plane")).seq(WhiteSpace()).seq(Value())
                .or(of("FINAL").seq(WhiteSpace()).seq(of("X-Plane")).seq(WhiteSpace()).seq(Value()));
    }

    Parser Value() {
        return noneOf("\r\n").plus();
    }


}
