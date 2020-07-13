package com.ogerardin.xplane.file.grammar;

import com.ogerardin.xplane.file.data.AcfFileData;
import com.ogerardin.xplane.file.data.Header;
import com.ogerardin.xplane.file.data.AcfProperty;
import lombok.extern.slf4j.Slf4j;
import org.parboiled.Action;
import org.parboiled.Context;
import org.parboiled.Rule;
import org.parboiled.annotations.SuppressSubnodes;

@Slf4j
public class AcfFileParser extends XPlaneFileParser {

    static final String REQUIRED_TYPE = "ACF";

    /**
     * Matches a full X-Plane com.ogerardin.xplane.file.
     * Upon successful match, pushes an instance of {@link AcfFileData}
     */
    @Override
    public Rule XPlaneFile() {
        return Sequence(
                Header(),
                ZeroOrMore(Newline()),
                Properties(),
                swap() && push(new AcfFileData((Header) pop(), (AcfFileData.AcfProperties) pop()))
//                Junk(),
//                EOI
        );
    }

    Rule Junk() {
        return ZeroOrMore(JunkLine());
    }

    Rule JunkLine() {
        return Sequence(ZeroOrMore(ANY), Newline());
    }

    /**
     * Matches a com.ogerardin.xplane.file header.
     * Upon successful match, pushes an instance of {@link Header}
     */
    Rule Header() {
        return Sequence(
                Origin(), WhiteSpace(), Newline(),
                VersionSpec(), WhiteSpace(), Newline(),
                FileType(), WhiteSpace(), Newline(),
                swap3() && push(new Header((String) pop(), (String) pop(), (String) pop()))
        );
    }

    /**
     * Matches a "properties" section.
     * Upon successful match, pushes an instance of {{@link AcfFileData.AcfProperties}}
     */
    @SuppressWarnings({"Convert2Lambda", "rawtypes"})
    Rule Properties() {
        return Sequence(
                "PROPERTIES_BEGIN", WhiteSpace(), Newline(),
                push(new AcfFileData.AcfProperties()),
                ZeroOrMore(
                        Sequence(
                                Property(),
                                new Action() {
                                    @Override
                                    public boolean run(Context context) {
                                        AcfProperty property = (AcfProperty) pop();
//                                        log.debug("Adding property {}", property);
                                        AcfFileData.AcfProperties properties = (AcfFileData.AcfProperties) peek();
                                        properties.put(property.getName(), property.getValue());
                                        return true;
                                    }
                                }
                        )
                ),
                "PROPERTIES_END", WhiteSpace(), Newline()
        );
    }

    /**
     * Matches a line with property name and value.
     * Upon successful match, pushes a {@link AcfProperty} instance
     */
    Rule Property() {
        return Sequence('P', ' ',
                PropertyName(), ' ',
                PropertyValue(),
                swap() && push(new AcfProperty((String) pop(), (String) pop())),
                Newline());
    }

    Rule Newline() {
        return Sequence(Optional('\r'), '\n');
    }

    /**
     * Matches a property value.
     * Upon successful match, pushes the value as a String.
     */
    @SuppressSubnodes
    Rule PropertyValue() {
        return Sequence(
//                OneOrMore(PropertyValueChar()),
                OneOrMore(NoneOf("\r\n")),
                push(match())
        );
    }

    Rule PropertyValueChar() {
        return CharRange(' ' , '~');
    }

    /**
     * Matches a property name.
     * Upon successful match, pushes the name as a String.
     */
    @SuppressSubnodes
    Rule PropertyName() {
        return Sequence(
                OneOrMore(PropertyNameChar()),
                push(match())
        );
    }

    Rule PropertyNameChar() {
        return FirstOf(Alphanumeric(), '_', ',', '/');
    }

    Rule Alphanumeric() {
        return FirstOf(Letter(), Digit());
    }

    Rule Spacechar() {
        return AnyOf(" \t");
    }

    Rule NotNewline() {
        return TestNot(AnyOf("\n\r"));
    }

    Rule FileType() {
        return Sequence(
//                Sequence(Letter(), Letter(), Letter()),
                REQUIRED_TYPE,
                push(match())
        );
    }

    Rule VersionSpec() {
        return Sequence(
                Version(), push(match()),
                " ",
                AnyOf("vV"), "ersion");
    }

    Rule WhiteSpace() {
        return ZeroOrMore(Spacechar());
    }

    Rule Version() {
        return Sequence(Digit(), Digit(), Digit(), Digit());
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
