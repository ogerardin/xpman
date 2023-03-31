package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.dat.DatFileData;
import com.ogerardin.xplane.file.data.dat.DatHeader;
import org.petitparser.parser.Parser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.*;
import static org.petitparser.parser.primitive.StringParser.ofIgnoringCase;

public class DatFileParser extends XPlaneFileParserBase<DatFileData> {

    @SuppressWarnings("unchecked")
    @Override
    public Parser XPlaneFile() {
        return DatHeader()
                .seq(JunkLine().star())
                .map((List<Object> input) -> new DatFileData((DatHeader) input.get(0)));
    }
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

    private Parser Version() {
        return digit().repeat(3, 4).flatten()
                .seq(ofIgnoringCase(" version").optional())
                .pick(0);

    }

    private Parser Cycle() {
        return ofIgnoringCase("data cycle ")
                .seq(digit().repeat(4,4).flatten())
                .pick(1);
    }

    private Parser Build() {
        return ofIgnoringCase("build ")
                .seq(digit().plus().flatten())
                .pick(1);
    }

    private Parser Metadata() {
        return ofIgnoringCase("metadata ")
                .seq(word().plus().flatten())
                .pick(1);
    }

}
