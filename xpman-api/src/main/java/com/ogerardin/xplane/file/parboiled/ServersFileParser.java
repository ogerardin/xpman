package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Rule;

/**
 * Parser for http://lookup-a.x-plane.com/_lookup_11_/server_list_11.txt
 * Currently only extracts beta version and final version.
 */
@Slf4j
public class ServersFileParser extends XPlaneFileParserBase {

    static final String REQUIRED_TYPE = "SERVERS";

    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(REQUIRED_TYPE),
                // VersionsFileData is pushed early on the stack and will be enriched by relevant rules
                push(new ServersFileData((Header) peek())),
                ZeroOrMore(
                        FirstOf(
                                // match line containing a version declaration
                                Sequence(VersionDecl(), Newline()),
                                // other
                                JunkLine()
                        )
                ),
                EOI
        );
    }

    Rule VersionDecl() {
        return FirstOf(
                Sequence("BETA", WhiteSpace(), "X-Plane", WhiteSpace(), Value(), (Action<?>) context -> {
                    String version = (String) pop();
                    ServersFileData data = (ServersFileData) peek();
                    data.setBetaVersion(version);
                    return true;
                }),
                Sequence("FINAL", WhiteSpace(), "X-Plane", WhiteSpace(), Value(), (Action<?>) context -> {
                    String version = (String) pop();
                    ServersFileData data = (ServersFileData) peek();
                    data.setFinalVersion(version);
                    return true;
                })
        );
    }

    Rule Value() {
        return Sequence(
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
    }

}
