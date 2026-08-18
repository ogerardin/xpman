package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.dat.DatFileData;
import com.ogerardin.xplane.file.data.dat.DatHeader;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.*;
import static org.petitparser.parser.primitive.StringParser.ofIgnoringCase;

/**
 * Parser for X-Plane DAT (navigation data) files.
 *
 * <p>This parser handles DAT files which contain navigation data such as waypoints,
 * airways, and airports. The parser currently focuses on the header portion.</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * DATFile = Header
 * Header = Origin Version [Cycle] [Build] [Metadata] Newline
 * </pre>
 *
 * <h2>Header Format</h2>
 * <pre>
 * I
 * 1100 version
 * data cycle 202401
 * build 1234
 * metadata somevalue
 * </pre>
 *
 * @author Olivier G.
 * @see DatFileData
 */
public class DatFileParser extends XPlaneFileParserBase<DatFileData> {

    /**
     * Parse a complete DAT file.
     *
     * @return parser for DAT files
     */
    @SuppressWarnings("unchecked")
    @Override
    public Parser XPlaneFile() {
        return DatHeader()
                .seq(JunkLine().star())
                .map((List<Object> input) -> new DatFileData((DatHeader) input.get(0)));
    }

    /**
     * Parse the DAT file header with origin, version, and optional metadata.
     *
     * @return parser for DAT headers
     */
    private Parser DatHeader() {
        return Origin()
                .seq(Version())
                .seq(anyOf(" ,-").star())
                .seq(Cycle().optional())
                .seq(anyOf(" ,-").star())
                .seq(Build().optional())
                .seq(anyOf(" ,-").star())
                .seq(Metadata().optional())
                .seq(anyOf(" ,-").star())
                .seq(JunkLine())
                .map((List<String> values) -> new DatHeader(values.get(0), values.get(1), values.get(3), values.get(5), values.get(7)));
    }

    /**
     * Parse the version number (3-4 digits).
     *
     * @return parser for version strings
     */
    private Parser Version() {
        return digit().repeat(3, 4).flatten()
                .seq(ofIgnoringCase(" version").optional())
                .pick(0);

    }

    /**
     * Parse the data cycle identifier (e.g., "data cycle 202401").
     *
     * @return parser for cycle identifiers
     */
    private Parser Cycle() {
        return ofIgnoringCase("data cycle ")
                .seq(digit().repeat(4,4).flatten())
                .pick(1);
    }

    /**
     * Parse the build number (e.g., "build 1234").
     *
     * @return parser for build numbers
     */
    private Parser Build() {
        return ofIgnoringCase("build ")
                .seq(digit().plus().flatten())
                .pick(1);
    }

    /**
     * Parse the metadata field (e.g., "metadata somevalue").
     *
     * @return parser for metadata
     */
    private Parser Metadata() {
        return ofIgnoringCase("metadata ")
                .seq(word().plus().flatten())
                .pick(1);
    }

}
