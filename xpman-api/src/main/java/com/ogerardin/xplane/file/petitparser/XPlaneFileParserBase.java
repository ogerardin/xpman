package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.XPlaneFileData;
import lombok.Getter;
import org.petitparser.context.Result;
import org.petitparser.parser.Parser;
import org.petitparser.parser.primitive.CharacterParser;

import java.util.List;

import static org.petitparser.parser.primitive.CharacterParser.anyOf;
import static org.petitparser.parser.primitive.CharacterParser.digit;
import static org.petitparser.parser.primitive.CharacterParser.letter;
import static org.petitparser.parser.primitive.CharacterParser.noneOf;
import static org.petitparser.parser.primitive.CharacterParser.of;
import static org.petitparser.parser.primitive.CharacterParser.range;
import static org.petitparser.parser.primitive.StringParser.of;

abstract class XPlaneFileParserBase<R extends XPlaneFileData> implements StringParser<R> {

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

    //
    // Common constructs
    //

    Parser JunkLine() {
        return noneOf("\r\n").star().seq(Newline());
    }

    Parser Header(String requiredType) {
        return Origin()
                .seq(VersionSpec())
                .seq(FileType(requiredType))
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
        return of(requiredType).flatten()
                .seq(Newline())
                .pick(0);
    }

    Parser VersionSpec() {
        return digit().repeat(3, 4).flatten()
                //.seq(of(" version").optional())
                //.seq(Newline())
                .seq(JunkLine())
                .pick(0);
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
        return anyOf("IA").flatten().seq(Newline()).pick(0);
    }

    //

}
