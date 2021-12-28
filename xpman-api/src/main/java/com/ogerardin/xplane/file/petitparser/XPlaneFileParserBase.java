package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.parser.primitive.CharacterParser;
import org.petitparser.parser.primitive.StringParser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.*;

abstract class XPlaneFileParserBase<R extends XPlaneFileData>  {

    @Getter(lazy = true)
    private final Parser parser = buildParser();

    private Parser buildParser() {
        return XPlaneFile();
    }

    public R parse(String contents) throws Exception {
        final Parser parser = getParser();
        final Result result = parser.parse(contents);
        return result.get();
    }

    protected abstract Parser XPlaneFile();

    Parser JunkLine() {
        return noneOf("\r\n").star().seq(Newline());
    }

    Parser Header(String requiredType) {
        return Origin().seq(JunkLine()).pick(0)
                .seq(VersionSpec().seq(JunkLine()).pick(0))
                .seq(FileType(requiredType).seq(Newline()).pick(0))
                .map((List<String> values) -> new Header(values.get(0), values.get(1), values.get(2)));
    }

    Parser Comment() {
        return of('#').seq(noneOf("\r\n").star());
    }

    Parser CommentLine() {
        return Comment().seq(Newline());
    }

    Parser Number() {
        return of('-').optional()
                .seq(digit().plus())
                .seq(of('.').seq(digit().plus()).optional());
    }

    Parser Newline() {
        return of('\r').optional().seq(of('\n'));
    }

    Parser Alphanumeric() {
        return letter().or(digit());
    }

    Parser Spacechar() {
        return anyOf(" \t");
    }

    Parser FileType(String requiredType) {
        return StringParser.of(requiredType);
    }

    Parser VersionSpec() {
        return Version();
    }

    Parser WhiteSpace() {
        return Spacechar().plus();
    }

    Parser Version() {
        return digit().plus().flatten();
    }

    Parser Letter() {
        return CharacterParser.range('a','z').or(range('A','Z'));
    }

//    Parser Digit() {
//        return CharRange('0', '9');
//    }

    Parser Origin() {
        return anyOf("IA").flatten();
    }

    //

}
