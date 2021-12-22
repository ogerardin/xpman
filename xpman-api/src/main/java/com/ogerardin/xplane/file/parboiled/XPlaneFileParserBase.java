package com.ogerardin.xplane.file.parboiled;

import com.ogerardin.xplane.file.data.Header;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

/**
 * Base class for all X-Plane file format parsers; uses <a href="https://github.com/sirthias/parboiled">Parboiled</a>.
 * The main rule is always {@link #XPlaneFile()}.
 * Type of values is {@link #Object} so we can push anything.
 */
public abstract class XPlaneFileParserBase extends BaseParser<Object> {

    public abstract Rule XPlaneFile();

    Rule JunkLine() {
        return Sequence(ZeroOrMore(NoneOf("\r\n")), Newline());
    }

    /**
     * Matches a X-Plane file header.
     * Upon successful match, pushes an instance of {@link Header}
     * @param requiredType
     */
    Rule Header(String requiredType) {
        return Sequence(
                Origin(), JunkLine(),
                VersionSpec(), JunkLine(),
                FileType(requiredType), JunkLine(),
                swap3() && push(new Header((String) pop(), (String) pop(), (String) pop()))
        );
    }

    Rule Comment() {
        return Sequence('#', ZeroOrMore(NoneOf("\r\n")));
    }

    Rule CommentLine() {
        return Sequence(Comment(), Newline());
    }

    @SuppressSubnodes
    Rule Number() {
        return Sequence(
                Optional('-'),
                OneOrMore(Digit()),
                Optional(
                        '.',
                        OneOrMore(Digit())
                )
        );
    }

    Rule Newline() {
        return Sequence(Optional('\r'), '\n');
    }

    Rule Alphanumeric() {
        return FirstOf(Letter(), Digit());
    }

    Rule Spacechar() {
        return AnyOf(" \t");
    }

    Rule FileType(String requiredType) {
        return Sequence(
                requiredType,
                push(match())
        );
    }

    Rule VersionSpec() {
        return Sequence(
                Version(), push(match())
        );
    }

    Rule WhiteSpace() {
        return OneOrMore(Spacechar());
    }

    Rule Version() {
        return OneOrMore(Digit());
    }

    Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule Digit() {
        return CharRange('0', '9');
    }

    Rule Origin() {
        return Sequence(
                AnyOf("IA"),
                push(match())
        );
    }
}
