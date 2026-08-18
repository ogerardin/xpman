package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.servers.ServersFileData;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.StringParser.of;

/**
 * Parser for X-Plane server list files containing version information.
 *
 * <p>This parser handles server list files which contain information about
 * available X-Plane versions (beta and final) for update checking.</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * ServersFile = Header("SERVERS") VersionDecl+ JunkLine*
 * VersionDecl = ("BETA" | "FINAL" | "FULL") " X-Plane " value Newline
 * </pre>
 *
 * <h2>Example Content</h2>
 * <pre>
 * I
 * 1200 version
 * SERVERS
 * BETA X-Plane 12.0.0-beta1
 * FINAL X-Plane 12.0.0
 * </pre>
 *
 * @author Olivier G.
 * @see ServersFileData
 */
public class ServersFileParser extends XPlaneFileParserBase<ServersFileData> {

    /**
     * Required file type for server list files.
     */
    static final String REQUIRED_TYPE = "SERVERS";

    /**
     * Parse a complete server list file.
     *
     * @return parser for server list files
     */
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

    /**
     * Parse a version declaration line.
     *
     * @return parser for version declarations
     */
    Parser VersionDecl() {
        return VersionType()
                .seq(of(" X-Plane "))
                .seq(Value())
                .seq(Newline())

                .map((List<Object> input)
                        -> new ServersFileData.Version((String) input.get(0), (String) input.get(2)));
    }

    /**
     * Parse the version type keyword.
     *
     * @return parser for version types
     */
    private Parser VersionType() {
        return of("BETA").or(of("FINAL")).or(of("FULL"));
    }

    /**
     * Parse a version value string.
     *
     * @return parser for version values
     */
    Parser Value() {
        return noneOf("\r\n").plus().flatten();
    }


}
