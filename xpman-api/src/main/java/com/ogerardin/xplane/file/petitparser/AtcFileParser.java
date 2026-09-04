package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.atc.AtcFileData;
import org.petitparser.parser.Parser;

/**
 * Parser for X-Plane atc.dat files.
 *
 * <p>Partial parser: only the standard header is parsed (origin, spec version, file type).</p>
 *
 * <h2>Grammar Overview</h2>
 * <pre>
 * ATCFile = Header
 * Header = Origin VersionSpec FileType
 * </pre>
 *
 * <p>See <a href="https://developer.x-plane.com/article/atc-dat/">atc.dat</a>
 * for the file format specification.</p>
 *
 * @see AtcFileData
 */
public class AtcFileParser extends XPlaneFileParserBase<AtcFileData> {

    @SuppressWarnings("unchecked")
    @Override
    public Parser XPlaneFile() {
        return Header("ATCFILE")
                .map(AtcFileData::new);
    }
}