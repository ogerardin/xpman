package com.ogerardin.xplane.file.petitparser;

import com.ogerardin.xplane.file.StringParser;
import com.ogerardin.xplane.file.data.Header;
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

// NOTE ON STYLE: in order to keep the syntax closer to usual grammar conventions where token and
// production names usually start with an upper case letter, methods that return a Parser will also
// follow this convention and bear the name of the token or production they are intended to parse.

/**
 * Base class for all X-Plane file parsers using PetitParser.
 *
 * <p>This class provides common parsing utilities and defines the Template Method
 * pattern for building file-specific parsers. Subclasses implement {@link #XPlaneFile()}
 * to define their grammar.</p>
 *
 * <h2>Design Notes</h2>
 * <ul>
 *   <li>Method names follow grammar conventions (uppercase for productions)</li>
 *   <li>Parsers are lazily initialized and cached</li>
 *   <li>Use {@link #Header(String)} to parse file headers with type validation</li>
 * </ul>
 *
 * <h2>Extension Guide</h2>
 * <p>To create a parser for a new file format:</p>
 * <ol>
 *   <li>Extend {@code XPlaneFileParserBase<YourDataType>}</li>
 *   <li>Implement {@link #XPlaneFile()} to define your grammar</li>
 *   <li>Use built-in parsers: {@link #Header(String)}, {@link #Number()}, {@link #Comment()}</li>
 *   <li>Add custom parsers for format-specific syntax</li>
 * </ol>
 *
 * <h3>Example</h3>
 * <pre>{@code
 * public class MyFileParser extends XPlaneFileParserBase<MyFileData> {
 *     static final String REQUIRED_TYPE = "MY_TYPE";
 *
 *     @Override
 *     public Parser XPlaneFile() {
 *         return Header(REQUIRED_TYPE)
 *             .seq(MyContent())
 *             .map((List<Object> input) -> {
 *                 Header header = (Header) input.get(0);
 *                 // parse content
 *                 return new MyFileData(header, content);
 *             });
 *     }
 *
 *     Parser MyContent() {
 *         return // your grammar rules
 *     }
 * }
 * }</pre>
 *
 * @param <R> the type of data produced by parsing
 * @author Olivier G.
 * @see StringParser
 */
abstract class XPlaneFileParserBase<R> implements StringParser<R> {

    /**
     * Lazily-initialized parser built by {@link #XPlaneFile()}.
     */
    @Getter(lazy = true)
    private final Parser parser = buildParser();

    private Parser buildParser() {
        return XPlaneFile();
    }

    /**
     * Parse the given string content.
     *
     * @param contents the file content to parse
     * @return the parsed data
     * @throws RuntimeException if parsing fails
     */
    public R parse(String contents) {
        final Parser parser = getParser();
        final Result result = parser.parse(contents);
        return result.get();
    }

    /**
     * Define the grammar for this file format.
     * Must return a parser that produces an instance of R.
     *
     * @return the root parser for this file format
     */
    protected abstract Parser XPlaneFile();

    //
    // Common constructs
    //

    /**
     * Parse a junk line (any content followed by newline).
     *
     * @return parser for junk lines
     */
    Parser JunkLine() {
        return noneOf("\r\n").star().seq(Newline());
    }

    /**
     * Parse a file header with origin, version, and file type.
     *
     * @param requiredType the expected file type (e.g., "OBJ", "ACF")
     * @return parser that produces a Header object
     */
    Parser Header(String requiredType) {
        return Origin()
                .seq(VersionSpec())
                .seq(FileType(requiredType))
                .map((List<String> values) -> new Header(values.get(0), values.get(1), values.get(2)));
    }

    /**
     * Parse a comment line (starts with #).
     *
     * @return parser for comments
     */
    Parser Comment() {
        return of('#').seq(noneOf("\r\n").star());
    }

    /**
     * Parse a comment line followed by newline.
     *
     * @return parser for comment lines
     */
    Parser CommentLine() {
        return Comment().seq(Newline());
    }

    /**
     * Parse a number (integer or decimal, optionally negative).
     *
     * @return parser for numbers
     */
    Parser Number() {
        return of('-').optional()
                .seq(digit().plus())
                .seq(of('.').seq(digit().plus()).optional());
    }

    /**
     * Parse a newline (CR, LF, or CRLF).
     *
     * @return parser for newlines
     */
    Parser Newline() {
        return of('\r').optional().seq(of('\n'));
    }

    /**
     * Parse an alphanumeric character.
     *
     * @return parser for alphanumeric characters
     */
    Parser Alphanumeric() {
        return letter().or(digit());
    }

    /**
     * Parse a space character (space or tab).
     *
     * @return parser for space characters
     */
    Parser Spacechar() {
        return anyOf(" \t");
    }

    /**
     * Parse a file type string and validate it.
     *
     * @param requiredType the expected file type
     * @return parser that validates and returns the file type
     */
    Parser FileType(String requiredType) {
        return of(requiredType).flatten()
                .seq(Newline())
                .pick(0);
    }

    /**
     * Parse a version specification (3-4 digits).
     *
     * @return parser for version strings
     */
    Parser VersionSpec() {
        return digit().repeat(3, 4).flatten()
                //.seq(of(" version").optional())
                //.seq(Newline())
                .seq(JunkLine())
                .pick(0);
    }

    /**
     * Parse one or more whitespace characters.
     *
     * @return parser for whitespace
     */
    Parser WhiteSpace() {
        return Spacechar().plus();
    }

//    Parser Version() {
//        return digit().plus().flatten();
//    }

    /**
     * Parse a letter (a-z or A-Z).
     *
     * @return parser for letters
     */
    Parser Letter() {
        return CharacterParser.range('a','z').or(range('A','Z'));
    }

//    Parser Digit() {
//        return CharRange('0', '9');
//    }

    /**
     * Parse the file origin ("I" or "A").
     *
     * @return parser for origin
     */
    Parser Origin() {
        return anyOf("IA").flatten().seq(Newline()).pick(0);
    }

    //

}
